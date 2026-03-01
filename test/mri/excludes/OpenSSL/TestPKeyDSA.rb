exclude :test_DSAPrivateKey, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_DSAPrivateKey_encrypted, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_PUBKEY, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_dup, 'passes except for setting (invalid) `key2.p + 1` as validation happens early'
exclude :test_export_password_length, 'needs investigation'
exclude :test_marshal, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_new_break, 'needs investigation'
exclude :test_new_empty, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271"
exclude :test_params, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_private, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, IllegalArgumentException: Bad sequence size: 3"
exclude :test_sign_verify, "work in progress"
exclude :test_sign_verify_raw, "TODO: sign_raw not implemented"
