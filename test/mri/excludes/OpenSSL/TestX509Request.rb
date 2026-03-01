exclude :test_attr, 'fails with <2> expected but was <0> [test_x509req.rb:94]'
exclude :test_dup, "work in progress"
exclude :test_eq, "work in progress"
exclude :test_marshal, "work in progress"
exclude :test_public_key, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, Digest must be retrieve from String parameter"
exclude :test_sign_and_verify, 'Version broke in recent rewrite'
exclude :test_sign_and_verify_ed25519, "work in progress"
exclude :test_sign_and_verify_nil_digest, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'generate_key' for module OpenSSL::PKey"
exclude :test_sign_and_verify_rsa_sha1, "work in progress"
exclude :test_sign_digest_instance, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, Digest must be retrieve from String parameter"
exclude :test_signature_algorithm, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, Digest must be retrieve from String parameter"
exclude :test_subject, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, Digest must be retrieve from String parameter"
exclude :test_version, 'Version broke in recent rewrite'
