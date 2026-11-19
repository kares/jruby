# super dispatch shapes from ActiveRecord 8.1
require_relative 'harness'

module ActiveRecord
  # model instantiation: Core#init_internals extended by a stack of included modules
  module Core
    def init_internals
      @readonly = false
    end
  end
  module Persistence; def init_internals; super; @previously_new_record = false; end; end
  module AutosaveAssociation; def init_internals; super; @_already_called = nil; end; end
  module Transactions; def init_internals; super; @_start_transaction_state = nil; end; end
  module TouchLater; def init_internals; super; @_defer_touch_attrs = nil; end; end
  class Base
    include Core, Persistence, AutosaveAssociation, Transactions, TouchLater
    public :init_internals
  end

  module ConnectionAdapters
    module DatabaseStatements
      def select_all(arel, name = nil, binds = [], preparable: nil, async: false) = arel
    end
    module QueryCache
      def select_all(arel, name = nil, binds = [], preparable: nil, async: false)
        super
      end
    end
    class AbstractAdapter
      include DatabaseStatements, QueryCache
    end
    module Mysql2
      # connection_adapters/mysql2/database_statements.rb -- anonymous splats, zsuper
      module DatabaseStatements
        def select_all(*, **)
          super
        end
      end
    end
    class Mysql2Adapter < AbstractAdapter
      include Mysql2::DatabaseStatements
    end
  end

  # relation/query_attribute.rb -- (...) initialize calling up into the class super
  class Attribute
    def initialize(name, value_before_type_cast, type, original_attribute = nil, value = nil)
      @name = name
    end
  end
  class QueryAttribute < Attribute
    def initialize(...)
      super
    end
  end
end

module Arel
  module Visitors
    class ToSql
      def visit_SelectStatement(o, collector) = collector
    end
    # arel/visitors/mysql.rb -- class super with explicit args
    class MySQL < ToSql
      def visit_SelectStatement(o, collector)
        super(o, collector)
      end
    end
  end
end

class PlainModel; def init_internals = @readonly = false; end

MODEL = ActiveRecord::Base.new
PLAIN = PlainModel.new
ADAPTER = ActiveRecord::ConnectionAdapters::Mysql2Adapter.new
VISITOR = Arel::Visitors::MySQL.new
SQL = 'SELECT 1'

RailsBench.run(
  'baseline: init_internals without super'                     => 'PLAIN.init_internals',
  'init_internals (4 x zsuper through included modules)'       => 'MODEL.init_internals',
  'Mysql2Adapter#select_all(sql) (*, ** zsuper -> zsuper)'     => 'ADAPTER.select_all(SQL)',
  'QueryAttribute.new(...) (forwarding zsuper)'                => "ActiveRecord::QueryAttribute.new('id', 1, nil)",
  'Attribute.new (no super, baseline for above)'               => "ActiveRecord::Attribute.new('id', 1, nil)",
  'MySQL#visit_SelectStatement (class super(o, c))'            => 'VISITOR.visit_SelectStatement(SQL, SQL)'
)
