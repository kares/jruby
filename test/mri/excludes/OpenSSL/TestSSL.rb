exclude :test_accept_errors_include_peeraddr, "work in progress"
exclude :test_add_certificate, "work in progress"
exclude :test_add_certificate_multiple_certs, "work in progress"
exclude :test_alpn_protocol_selection_ary, 'SSLContext#alpn_select_cb= not supported'
exclude :test_alpn_protocol_selection_cancel, 'SSLContext#alpn_select_cb= not supported'
exclude :test_ciphers_method_bogus_csuite, "work in progress"
exclude :test_ciphers_method_frozen_object, "work in progress"
exclude :test_ciphers_method_tls_connection, "work in progress"
exclude :test_ciphersuites_method_bogus_csuite, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, missing method on SSLContext"
exclude :test_ciphersuites_method_frozen_object, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271"
exclude :test_ciphersuites_method_nil_argument, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, missing method on SSLContext"
exclude :test_ciphersuites_method_tls_connection, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, missing method on SSLContext"
exclude :test_client_ca, 'needs investigation'
exclude :test_client_cert_cb_ignore_error, "work in progress"
exclude :test_client_sigalgs, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'client_sigalgs=' for an instance of OpenSSL::SSL::SSLContext"
exclude :test_connect_certificate_verify_failed_exception_message, "work in progress"
exclude :test_connect_works_when_setting_dh_callback_to_nil, "work in progress"
exclude :test_ctx_options_config, "work in progress"
exclude :test_ctx_setup_invalid, 'works sufficiently - low priority'
exclude :test_dup, "work in progress"
exclude :test_ecdh_curves_tls12, "work in progress"
exclude :test_ecdh_curves_tls13, "work in progress"
exclude :test_exception_in_verify_callback_is_ignored, "work in progress"
exclude :test_export_keying_material, "work in progress"
exclude :test_extra_chain_cert_auto_chain, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271"
exclude :test_finished_messages, 'hangs for a long time and then fails'
exclude :test_freeze_calls_setup, "work in progress"
exclude :test_get_ephemeral_key, "work in progress"
exclude :test_getbyte, "work in progress"
exclude :test_keylog_cb, "work in progress"
exclude :test_minmax_version_system_default, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, TLS version mismatch"
exclude :test_npn_advertised_protocol_too_long, 'SSLContext#npn_protocols= not supported'
exclude :test_npn_protocol_selection_ary, 'SSLContext#npn_protocols= not supported'
exclude :test_npn_protocol_selection_cancel, 'SSLContext#npn_protocols= not supported'
exclude :test_npn_protocol_selection_enum, 'SSLContext#npn_protocols= not supported'
exclude :test_npn_selected_protocol_too_long, 'SSLContext#npn_protocols= not supported'
exclude :test_options_defaults_to_OP_ALL_on, 'needs investigation'
exclude :test_options_setting_nil_means_all, 'needs investigation'
exclude :test_post_connect_check_with_anon_ciphers, 'needs investigation'
exclude :test_read_nonblock_without_session, 'HANGS'
exclude :test_readbyte, "work in progress"
exclude :test_renegotiation_cb, 'SSLContext#renegotiation_cb= not supported'
exclude :test_respect_system_default_min, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271"
exclude :test_s_read_der_then_pem, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'private_to_pem' for an instance of OpenSSL::PKey::RSA"
exclude :test_s_read_passphrase, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'private_to_pem' for an instance of OpenSSL::PKey::RSA"
exclude :test_s_read_pem_unknown_block, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'private_to_pem' for an instance of OpenSSL::PKey::RSA"
exclude :test_security_level, "work in progress"
exclude :test_servername_cb, 'Errno::EOPNOTSUPP: Operation not supported - Socket.socketpair only supports streaming UNIX sockets'
exclude :test_servername_cb_calls_setup_on_returned_ctx, 'Errno::EOPNOTSUPP: Operation not supported - Socket.socketpair only supports streaming UNIX sockets'
exclude :test_servername_cb_can_return_nil, 'Errno::EOPNOTSUPP: Operation not supported - Socket.socketpair only supports streaming UNIX sockets'
exclude :test_servername_cb_raises_an_exception_on_unknown_objects, 'Errno::EOPNOTSUPP: Operation not supported - Socket.socketpair only supports streaming UNIX sockets'
exclude :test_servername_cb_sets_context_on_the_socket, 'Errno::EOPNOTSUPP: Operation not supported - Socket.socketpair only supports streaming UNIX sockets'
exclude :test_set_groups_tls12, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, OpenSSL::SSL::SSLError: no cipher match"
exclude :test_set_groups_tls13, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'groups=' for an instance of OpenSSL::SSL::SSLContext"
exclude :test_sigalgs, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, undefined method 'add_certificate' for an instance of OpenSSL::SSL::SSLContext"
exclude :test_socket_close_write, "work in progress"
exclude :test_ssl_sysread_blocking_error, 'works except JRuby-OpenSSL does not raise TypeError on SSLSocket#sysread(4, exception: false)'
exclude :test_ssl_with_server_cert, "work in progress"
exclude :test_sslctx_set_params, "work in progress"
exclude :test_sysread_and_syswrite, "new failure with Ruby 4.0 tests, tested on MacOS, https://github.com/jruby/jruby/issues/9271, appears to hang and timeout"
exclude :test_tlsext_hostname, "work in progress"
exclude :test_tmp_dh, "work in progress"
exclude :test_tmp_dh_callback, "work in progress"
exclude :test_unstarted_session, "work in progress"
exclude :test_verify_certificate_identity, 'needs investigation'
exclude :test_verify_hostname_failure_error_code, "work in progress"
exclude :test_verify_hostname_on_connect, "work in progress"
exclude :test_verify_mode_client_cert_required, "work in progress"
exclude :test_verify_mode_default, "work in progress"
exclude :test_verify_result, "work in progress"
