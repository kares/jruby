# Module#=== / kind_of? / is_a? / instance_of? against Arel's class hierarchy (ActiveRecord 8.1)
#
# ancestor chains copied from a Rails 8.1 process -- ActiveSupport and json mix modules into
# Object, String, ..., so a miss walks 10+ entries
require_relative 'harness'

module ActiveSupport; module ToJsonWithActiveSupportEncoder; end; module Tryable; end; end
module JSON; module Generator; module GeneratorMethods; module Object; end; module String; end; end; end; end
class Object
  include ActiveSupport::Tryable, JSON::Generator::GeneratorMethods::Object, ActiveSupport::ToJsonWithActiveSupportEncoder
end
class String
  include Comparable, JSON::Generator::GeneratorMethods::String, ActiveSupport::ToJsonWithActiveSupportEncoder
end

module Arel
  module FactoryMethods; end
  module Expressions; end
  module Predications; end
  module AliasPredication; end
  module OrderPredications; end
  module Math; end
  module Nodes
    module FetchAttribute; end
    class Node; include FactoryMethods; end
    class NodeExpression < Node; include Expressions, Predications, AliasPredication, OrderPredications, Math; end
    class Binary < NodeExpression; end
    class Equality < Binary; include FetchAttribute; end
    class In < Binary; include FetchAttribute; end
    class Unary < NodeExpression; end
    class Quoted < Unary; end
    class Casted < NodeExpression; end
    class SqlLiteral < String; include Expressions, Predications, AliasPredication, OrderPredications; end

    # arel/nodes/casted.rb
    def self.build_quoted(other, attribute = nil)
      case other
      when Arel::Nodes::Node, Arel::Attributes::Attribute, Arel::Table, Arel::SelectManager, Arel::Nodes::SqlLiteral, ActiveModel::Attribute
        other
      else
        case attribute
        when Arel::Attributes::Attribute
          :casted
        else
          :quoted
        end
      end
    end
  end
  module Attributes
    class Attribute < Struct.new(:relation, :name); include Expressions, Predications, AliasPredication, OrderPredications, Math; end
  end
  Attribute = Attributes::Attribute
  class Table; include FactoryMethods, AliasPredication; end
  class SelectManager; end

  # arel.rb
  def self.arel_node?(value)
    value.is_a?(Arel::Nodes::Node) || value.is_a?(Arel::Attribute) || value.is_a?(Arel::Nodes::SqlLiteral)
  end
end
module ActiveModel; class Attribute; end; end

# arel/visitors/to_sql.rb
def quote_table_name(name)
  return name if Arel::Nodes::SqlLiteral === name
  name
end

# active_record/relation/where_clause.rb (contradiction?, per predicate)
def contradiction?(x)
  case x
  when Arel::Nodes::In
    Array === x
  when Arel::Nodes::Equality
    false
  end
end

TABLE_NAME = 'mysql2_bench'
EQUALITY = Arel::Nodes::Equality.new
LITERAL = Arel::Nodes::SqlLiteral.new('*')
OBJ = Object.new

RailsBench.run(
  'baseline: OBJ.equal?(OBJ)'                                 => 'OBJ.equal?(OBJ)',
  'quote_table_name: SqlLiteral === String (miss)'            => 'quote_table_name(TABLE_NAME)',
  'quote_table_name: SqlLiteral === SqlLiteral (hit)'         => 'quote_table_name(LITERAL)',
  'build_quoted(String) (6 class misses)'                     => "Arel::Nodes.build_quoted('char50')",
  'build_quoted(Equality) (hit on first)'                     => 'Arel::Nodes.build_quoted(EQUALITY)',
  'contradiction?(Equality) (case/when: miss + hit)'          => 'contradiction?(EQUALITY)',
  'arel_node?(String) (3 is_a? misses)'                       => "Arel.arel_node?('char50')",
  'Equality.is_a?(Arel::Nodes::Node) (class, 9 up)'           => 'EQUALITY.is_a?(Arel::Nodes::Node)',
  'Equality.kind_of?(Arel::Predications) (module)'            => 'EQUALITY.kind_of?(Arel::Predications)',
  'Equality.instance_of?(Arel::Nodes::Equality)'              => 'EQUALITY.instance_of?(Arel::Nodes::Equality)',
  'Arel::Nodes::Node === Equality'                            => 'Arel::Nodes::Node === EQUALITY'
)
