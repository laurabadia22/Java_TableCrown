<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/auth.css">
</#assign>

<#assign extra_js>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('.auth-toggle-password').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var input = document.getElementById(btn.getAttribute('data-target'));
                    var icon = btn.querySelector('i');
                    var mostraOra = input.getAttribute('type') === 'password';
                    input.setAttribute('type', mostraOra ? 'text' : 'password');
                    icon.classList.toggle('ti-eye', !mostraOra);
                    icon.classList.toggle('ti-eye-off', mostraOra);
                    btn.setAttribute('aria-label', mostraOra ? 'Nascondi password' : 'Mostra password');
                });
            });
        });
    </script>
</#assign>

<@layout.page page_title="Registrazione - TableCrown" extra_css=extra_css extra_js=extra_js>
    <div class="auth-container">
        <div class="container">
            <div class="auth-card auth-card-wide">

                <div class="auth-card-header">
                    <div class="auth-icon">
                        <i class="ti ti-user-plus"></i>
                    </div>
                    <h1 class="auth-title">Crea il tuo account</h1>
                    <p class="auth-subtitle">Unisciti alla community TableCrown</p>
                </div>

                <#if flash_message??>
                    <div class="alert alert-${flash_type!'danger'}">
                        ${flash_message}
                    </div>
                </#if>

                <form class="auth-form" action="${base_url!''}/registrazione" method="post" id="register-form">

                    <div class="auth-field">
                        <label for="nickname" class="auth-label">Nickname</label>
                        <div class="auth-input-wrapper">
                            <i class="ti ti-user"></i>
                            <input class="input auth-input"
                                   type="text"
                                   name="nome"
                                   id="nickname"
                                   placeholder="MastroDadi92"
                                   required
                                   minlength="3"
                                   maxlength="30"
                                   autocomplete="username">
                        </div>
                    </div>

                    <div class="auth-fields-row">
                        <div class="auth-field">
                            <label for="email" class="auth-label">Email</label>
                            <div class="auth-input-wrapper">
                                <i class="ti ti-mail"></i>
                                <input class="input auth-input"
                                       type="email"
                                       name="email"
                                       id="email"
                                       placeholder="nome@esempio.it"
                                       required
                                       autocomplete="email">
                            </div>
                        </div>

                        <div class="auth-field">
                            <label for="data_nascita" class="auth-label">Data di nascita</label>
                            <div class="auth-input-wrapper">
                                <i class="ti ti-calendar"></i>
                                <input class="input auth-input"
                                       type="date"
                                       name="data_nascita"
                                       id="data_nascita"
                                       required
                                       autocomplete="bday">
                            </div>
                        </div>
                    </div>

                    <div class="auth-fields-row">
                        <div class="auth-field">
                            <label for="password" class="auth-label">Password</label>
                            <div class="auth-input-wrapper">
                                <i class="ti ti-lock"></i>
                                <input class="input auth-input"
                                       type="password"
                                       name="password"
                                       id="password"
                                       placeholder="Min. 8 caratteri"
                                       required
                                       minlength="8"
                                       autocomplete="new-password">
                                <button type="button" class="auth-toggle-password" data-target="password" aria-label="Mostra password">
                                    <i class="ti ti-eye"></i>
                                </button>
                            </div>
                        </div>

                        <div class="auth-field">
                            <label for="conferma_password" class="auth-label">Conferma Password</label>
                            <div class="auth-input-wrapper">
                                <i class="ti ti-lock-check"></i>
                                <input class="input auth-input"
                                       type="password"
                                       name="conferma_password"
                                       id="conferma_password"
                                       placeholder="Ripeti password"
                                       required
                                       minlength="8"
                                       autocomplete="new-password">
                                <button type="button" class="auth-toggle-password" data-target="conferma_password" aria-label="Mostra password">
                                    <i class="ti ti-eye"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <label class="auth-checkbox-label auth-checkbox-terms">
                        <input type="checkbox" name="accetta_termini" value="1" required>
                        <span>Accetto i <a href="${base_url!''}/termini" class="auth-link-inline" target="_blank">Termini di Servizio</a> e la <a href="${base_url!''}/privacy" class="auth-link-inline" target="_blank">Privacy Policy</a></span>
                    </label>

                    <button class="button auth-submit-btn" type="submit">
                        <i class="ti ti-user-plus"></i> Crea account
                    </button>

                </form>

                <div class="auth-divider">
                    <span>oppure</span>
                </div>

                <p class="auth-footer-text">
                    Hai già un account?
                    <a href="${base_url!''}/accedi" class="auth-link">Accedi</a>
                </p>

            </div>
        </div>
    </div>
</@layout.page>