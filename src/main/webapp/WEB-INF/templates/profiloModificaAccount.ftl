<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/profiloModificaAccount.css">
</#assign>

<#assign extra_js>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // ── Preview avatar al cambio file (facoltativo, puramente estetico) ──
            var avatarInput = document.getElementById('modifica-account-avatar-input');
            var avatarPreview = document.getElementById('modifica-account-avatar-preview');

            if (avatarInput && avatarPreview) {
                avatarInput.addEventListener('change', function (e) {
                    var file = e.target.files[0];
                    if (!file) return;

                    var reader = new FileReader();
                    reader.onload = function (ev) {
                        if (avatarPreview.tagName === 'IMG') {
                            avatarPreview.src = ev.target.result;
                        } else {
                            var img = document.createElement('img');
                            img.src = ev.target.result;
                            img.className = 'modifica-account-avatar-img';
                            img.id = 'modifica-account-avatar-preview';
                            avatarPreview.replaceWith(img);
                            avatarPreview = img;
                        }
                    };
                    reader.readAsDataURL(file);
                });
            }
        });
    </script>
</#assign>

<@layout.page page_title="Modifica Account - TableCrown" extra_css=extra_css extra_js=extra_js>

    <div class="modifica-account-container">
        <div class="container">

            <div class="modifica-account-topbar">
                <a href="${base_url}/profilo" class="modifica-account-back-link">
                    <i class="ti ti-arrow-left"></i> Torna all'Area Personale
                </a>

                <div class="modifica-account-topbar-actions">
                    <a href="${base_url}/logout" class="modifica-account-logout">
                        <i class="ti ti-logout"></i> Log-out
                    </a>
                </div>
            </div>

            <#-- ── FORM PRINCIPALE: avatar + nome + email ── -->
            <form action="${base_url}/profilo/modifica" method="POST" enctype="multipart/form-data" class="modifica-account-form">

                <div class="modifica-account-avatar-wrap">
                    <div class="modifica-account-avatar-circle">
                        <div class="modifica-account-avatar">
                            <#if immagineUtente?has_content>
                                <img src="${base_url}/${immagineUtente}"                                     alt="${nomeUtente!''?html}"
                                     class="modifica-account-avatar-img"
                                     id="modifica-account-avatar-preview">
                            <#else>
                                <span class="modifica-account-avatar-placeholder" id="modifica-account-avatar-preview">IMG</span>
                            </#if>
                        </div>

                        <label for="modifica-account-avatar-input" class="modifica-account-avatar-edit">
                            <i class="ti ti-pencil"></i>
                        </label>
                    </div>

                    <input type="file"
                           id="modifica-account-avatar-input"
                           name="img_profilo"
                           accept="image/*"
                           class="modifica-account-avatar-input">

                    <span class="modifica-account-avatar-hint">Tocca la matita per cambiare foto</span>
                </div>

                <div class="modifica-account-card">
                    <div class="modifica-account-fields">

                        <div class="modifica-account-field">
                            <label for="nome" class="modifica-account-label">
                                <i class="ti ti-user"></i> Nickname
                            </label>
                            <input type="text" id="nome" name="nome" class="modifica-account-input" value="${nomeUtente!''?html}">
                        </div>

                        <div class="modifica-account-field">
                            <label for="email" class="modifica-account-label">
                                <i class="ti ti-mail"></i> Email
                            </label>
                            <input type="email" id="email" name="email" class="modifica-account-input" value="${emailUtente!''?html}">
                        </div>

                    </div>
                </div>

                <div class="modifica-account-actions">
                    <button type="submit" class="modifica-account-btn-primary">
                        <i class="ti ti-check"></i> Salva
                    </button>
                </div>

            </form>

            <#-- ── MODIFICA PASSWORD (<details>, no JS) ── -->
            <details class="modifica-account-details">
                <summary class="modifica-account-summary">
                    <i class="ti ti-lock"></i> Modifica Password
                </summary>

                <form action="${base_url}/profilo/modifica/password" method="POST" class="modifica-account-password-form">
                    <div class="modifica-account-field">
                        <label for="vecchia_password" class="modifica-account-label">Password Vecchia</label>
                        <input type="password" id="vecchia_password" name="vecchia_password" class="modifica-account-input" required>
                    </div>

                    <div class="modifica-account-field">
                        <label for="nuova_password" class="modifica-account-label">Password Nuova</label>
                        <input type="password" id="nuova_password" name="nuova_password" class="modifica-account-input" minlength="8" required>
                    </div>

                    <div class="modifica-account-field">
                        <label for="conferma_password" class="modifica-account-label">Conferma Password Nuova</label>
                        <input type="password" id="conferma_password" name="conferma_password" class="modifica-account-input" minlength="8" required>
                    </div>

                    <div class="modifica-account-actions">
                        <button type="submit" class="modifica-account-btn-primary">
                            <i class="ti ti-check"></i> Salva Password
                        </button>
                    </div>
                </form>
            </details>

            <#-- ── ELIMINA ACCOUNT (<details>, no JS) ── -->
            <details class="modifica-account-details modifica-account-details-danger" id="modifica-account-elimina">
                <summary class="modifica-account-summary modifica-account-summary-danger">
                    <i class="ti ti-alert-triangle"></i> Elimina Account
                </summary>

                <p class="modifica-account-danger-testo">
                    Questa azione è irreversibile: il tuo account e tutti i dati associati verranno eliminati definitivamente.
                </p>

                <form action="${base_url}/profilo/modifica/elimina" method="POST" class="modifica-account-password-form"
                      onsubmit="return confirm('Sei sicuro di voler eliminare definitivamente il tuo account?');">
                    <div class="modifica-account-field">
                        <label for="password" class="modifica-account-label">Inserisci la password per confermare</label>
                        <input type="password" id="password" name="password" class="modifica-account-input" required>
                    </div>

                    <div class="modifica-account-actions">
                        <button type="submit" class="modifica-account-btn-danger">
                            <i class="ti ti-trash"></i> Elimina Account Definitivamente
                        </button>
                    </div>
                </form>
            </details>

        </div>
    </div>

</@layout.page>