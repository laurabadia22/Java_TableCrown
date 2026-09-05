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

<@layout.page page_title="Accedi - TableCrown" extra_css=extra_css extra_js=extra_js>
    <div class="auth-container">
        <div class="container">
            <div class="auth-card">

                <div class="auth-card-header">
                    <div class="auth-icon">
                        <i class="ti ti-login"></i>
                    </div>
                    <h1 class="auth-title">Bentornato</h1>
                    <p class="auth-subtitle">Accedi al tuo account TableCrown</p>
                </div>

                <#if flash_message??>
                    <div class="alert alert-${flash_type!'danger'}">
                        ${flash_message}
                    </div>
                </#if>

                <form class="auth-form" action="${base_url!''}/login" method="post" id="login-form">

                    <#if redirect_to?? && redirect_to?has_content>
                        <input type="hidden" name="redirect_to" value="${redirect_to}">
                    </#if>

                    <div class="auth-field">
                        <label for="email" class="auth-label">Email</label>
                        <div class="auth-input-wrapper">
                            <i class="ti ti-mail"></i>
                            <input class="input auth-input"
                                   type="email"
                                   name="email"
                                   id="email"
                                   placeholder="nome@esempio.it"
                                   value="${email_value!''}"
                                   required
                                   autocomplete="email">
                        </div>
                    </div>

                    <div class="auth-field">
                        <label for="password" class="auth-label">Password</label>
                        <div class="auth-input-wrapper">
                            <i class="ti ti-lock"></i>
                            <input class="input auth-input"
                                   type="password"
                                   name="password"
                                   id="password"
                                   placeholder="••••••••"
                                   required
                                   autocomplete="current-password">
                            <button type="button" class="auth-toggle-password" data-target="password" aria-label="Mostra password">
                                <i class="ti ti-eye"></i>
                            </button>
                        </div>
                    </div>

                    <div class="auth-row-between">
                        <a href="${base_url!''}/password-dimenticata" class="auth-link-inline">Password dimenticata?</a>
                    </div>

                    <button class="button auth-submit-btn" type="submit">
                        <i class="ti ti-login"></i> Accedi
                    </button>

                </form>

                <div class="auth-divider">
                    <span>oppure</span>
                </div>

                <p class="auth-footer-text">
                    Non hai un account?
                    <a href="${base_url!''}/registrati" class="auth-link">Registrati ora</a>
                </p>

            </div>
        </div>
    </div>
</@layout.page>