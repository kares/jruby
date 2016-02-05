# it is war-file
packaging 'war'

# get jruby dependencies
properties( 'jruby.version' => '@project.version@',
            'jruby.plugins.version' => '1.0.10',
            'project.build.sourceEncoding' => 'utf-8' )

pom( 'org.jruby:jruby', '${jruby.version}' )

# a gem to be used
gem 'virtus', '0.5.5'

repository( :url => 'https://otto.takari.io/content/repositories/rubygems/maven/releases',
            :id => 'rubygems-releases' )

jruby_plugin :gem, :includeRubygemsInResources => true do
  execute_goal :initialize
end

execute 'jrubydir', 'initialize' do |ctx|
  require 'jruby/commands'
  JRuby::Commands.generate_dir_info( ctx.project.build.directory.to_pathname + '/rubygems' )
end

# ruby-maven will dump an equivalent pom.xml
properties( 'tesla.dump.pom' => 'pom.xml',
            'jruby.home' => '../../../../../' )

plugin( 'org.wildfly.plugins:wildfly-maven-plugin:1.0.2.Final' ) do
  execute_goals( :start,
                 :id => 'wildfly-start',
                 :phase => 'pre-integration-test' )
  execute_goals( :shutdown,
                 :id => 'wildfly-stop',
                 :phase => 'post-integration-test' )
end


build do
  final_name '${project.artifactId}'
end

# download files during the tests
execute 'download', :phase => 'integration-test' do
  require 'open-uri'
  dir = Dir[ 'target/wildfly-run/*' ].first
  FileUtils.cp( 'target/j2ee_wildfly.war', dir + '/standalone/deployments/packed.war' )
  FileUtils.cp_r( 'target/j2ee_wildfly', dir + '/standalone/deployments/unpacked.war' )
  FileUtils.touch( dir + '/standalone/deployments/unpacked.war.dodeploy' )

  # packed application
  count = 10
  begin
    sleep 1
    result = open( 'http://localhost:8080/packed/index.jsp' ).string
  rescue
    count -= 1
    retry if count > 0
  end
  File.open( 'result1', 'w' ) { |f| f.puts result }

  # unpacked application
  count = 10
  begin
    sleep 1
    result = open( 'http://localhost:8080/unpacked/index.jsp' ).string
  rescue
    count -= 1
    retry if count > 0
  end
  File.open( 'result2', 'w' ) { |f| f.puts result }
end

# verify the downloads
execute 'check download', :phase => :verify do
  [ 'result1', 'result2' ].each do |r|
    result = File.read( r )
    expected = 'hello world:'
    unless result.match( /#{expected}/ )
      raise "missed expected string in download: #{expected}"
    end
    expected = 'uri:classloader:/gems/backports-'
    unless result.match( /#{expected}/ )
      raise "missed expected string in download: #{expected}"
    end
    expected = 'snakeyaml-1.13.0'
    unless result.match( /#{expected}/ )
      raise "missed expected string in download: #{expected}"
    end
  end
end
