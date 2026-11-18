# respond_to? probes ActiveRecord 8.1 / ActiveSupport run while building a query
require_relative 'harness'

class Object
  # active_support/core_ext/object/blank.rb
  def blank?
    respond_to?(:empty?) ? !!empty? : false
  end
end

class QueryAttribute
  def value_for_database = 'char50'
end

class SubstituteBinds
  # arel/collectors/substitute_binds.rb
  def add_bind(bind)
    bind = bind.value_for_database if bind.respond_to?(:value_for_database)
    bind
  end
end

class StringType; end

COLLECTOR = SubstituteBinds.new
BIND = QueryAttribute.new
TYPE = StringType.new
OBJ = Object.new

RailsBench.run(
  'baseline: OBJ.equal?(OBJ)'                                  => 'OBJ.equal?(OBJ)',
  'Object#blank? (respond_to?(:empty?) miss)'                  => 'OBJ.blank?',
  'add_bind(QueryAttribute) (respond_to? hit)'                 => 'COLLECTOR.add_bind(BIND)',
  "add_bind('str') (respond_to? miss)"                         => "COLLECTOR.add_bind('str')",
  # relation/query_attribute.rb#nil?
  'type.respond_to?(:subtype) || type.respond_to?(:normalizer)' => 'TYPE.respond_to?(:subtype) || TYPE.respond_to?(:normalizer)'
)
