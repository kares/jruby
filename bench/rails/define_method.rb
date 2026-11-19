# class_attribute readers are define_method closures (active_support/class_attribute.rb),
# called hundreds of times per query (table_name, primary_key, default_scopes, ...)
require_relative 'harness'

module ClassAttribute
  def self.redefine(owner, name, value)
    owner.singleton_class.define_method(name) { value }
  end
end

class Model
  ClassAttribute.redefine(self, :default_scopes, [])
  ClassAttribute.redefine(self, :store_full_class_name, true)

  def self.plain_reader = true
  class << self; attr_reader :ivar_reader; end
  @ivar_reader = true
end

RailsBench.run(
  'baseline: def plain_reader = true'                          => 'Model.plain_reader',
  'attr_reader on the singleton'                               => 'Model.ivar_reader',
  'class_attribute reader (define_method { value })'           => 'Model.store_full_class_name',
  'class_attribute reader returning an Array'                  => 'Model.default_scopes'
)
