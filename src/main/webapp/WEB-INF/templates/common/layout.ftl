<#macro page page_title="TableCrown" extra_css="" extra_js="" current_page="" current_subpage="" breadcrumbs=[]>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">

    <title>${page_title}</title>
    <link rel="icon" type="image/png" href="${base_url}/public/img/favicon.png">

    <!-- CSS -->
    <link rel="stylesheet" href="${base_url}/public/plugins/bulma/bulma.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@3.19.0/dist/tabler-icons.min.css">

    <link rel="stylesheet" href="${base_url}/public/css/base.css">
    <link rel="stylesheet" href="${base_url}/public/css/layout.css">

    <!-- CSS Extra inserito dalle pagine figlie -->
    ${extra_css}
</head>
<body>

    <!-- ========================================== -->
    <!-- HEADER TABLECROWN                          -->
    <!-- ========================================== -->
    <nav class="navbar navigation" role="navigation" aria-label="navigazione principale">
        <div class="container is-fluid px-5">

            <div class="navbar-row-top-clean">

                <div class="navbar-brand-mobile-only">
                    <a role="button" class="navbar-burger" id="navbar-burger" aria-label="Apri menu" aria-expanded="false" data-target="navbar-menu-custom">
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                    </a>
                </div>

                <div id="navbar-menu-custom" class="navbar-menu-custom-links">

                    <!-- LOGO -->
                    <a class="navbar-logo" href="${base_url}">
                       <img src="${base_url}/public/img/favicon.png" alt="TableCrown" class="navbar-logo-img">
                    </a>

                    <div class="navbar-center-links">

                        <div class="has-catalogo-dropdown" id="catalogo-dropdown">
                            <span class="navbar-item catalogo-label<#if current_page?? && current_page == 'catalogo'> is-active</#if>">
                                Catalogo
                                <i class="ti ti-chevron-down navbar-dropdown-arrow"></i>
                            </span>
                            <div class="navbar-dropdown-catalogo">
                                <a class="navbar-item<#if current_subpage?? && current_subpage == 'giochi-da-tavolo'> is-active</#if>" href="${base_url}/catalogo/giochi-da-tavolo">
                                    <i class="ti ti-dice-5"></i> Giochi da tavolo
                                </a>
                                <a class="navbar-item<#if current_subpage?? && current_subpage == 'bustine'> is-active</#if>" href="${base_url}/catalogo/bustine">
                                    <i class="ti ti-cards"></i> Bustine
                                </a>
                                <a class="navbar-item<#if current_subpage?? && current_subpage == 'porta-dadi'> is-active</#if>" href="${base_url}/catalogo/porta-dadi">
                                    <i class="ti ti-package"></i> Porta dadi
                                </a>
                            </div>
                        </div>

                        <a class="navbar-item<#if current_page?? && current_page == 'offerte'> is-active</#if>" href="${base_url}/offerte">Offerte</a>

                    </div>

                    <div class="navbar-end-actions">

                        <!-- Accedi / Account -->
                        <#if utente??>
                            <div class="navbar-item has-dropdown" id="user-dropdown">
                                <a class="navbar-link navbar-user-link">
                                    <i class="ti ti-user-circle navbar-icon"></i>
                                    <span class="navbar-username">${utente.nome?html}</span>
                                </a>
                                <div class="navbar-dropdown is-right">
                                    <a class="navbar-item" href="${base_url}/profilo"><i class="ti ti-user"></i> Il mio account</a>
                                    <a class="navbar-item" href="${base_url}/profilo/ordini"><i class="ti ti-package"></i> I miei ordini</a>
                                    <a class="navbar-item" href="${base_url}/profilo/indirizzi"><i class="ti ti-map-pin"></i> I miei indirizzi</a>
                                    <a class="navbar-item" href="${base_url}/profilo/pagamenti"><i class="ti ti-credit-card"></i> Metodi di pagamento</a>
                                    <hr class="navbar-divider">
                                    <a class="navbar-item navbar-logout" href="${base_url}/logout"><i class="ti ti-logout"></i> Logout</a>
                                </div>
                            </div>
                        <#else>
                            <a class="header-top-link" href="${base_url}/accedi">Accedi</a>
                        </#if>

                        <!-- Wishlist -->
                        <#if utente??>
                            <a class="header-top-link" href="${base_url}/profilo/wishlist" title="La mia wishlist">
                                <i class="ti ti-heart navbar-icon"></i>
                                <span class="header-top-label">Wishlist</span>
                            </a>
                        <#else>
                            <a class="header-top-link nav-protected" href="#" title="La mia wishlist">
                                <i class="ti ti-heart navbar-icon"></i>
                                <span class="header-top-label">Wishlist</span>
                            </a>
                        </#if>

                        <!-- Carrello -->
                        <#if utente??>
                            <a class="header-top-link" href="${base_url}/carrello" title="Carrello">
                                <i class="ti ti-shopping-cart navbar-icon"></i>
                                <span class="header-top-label">Carrello</span>
                                <#if cart_count?? && (cart_count > 0)>
                                    <span class="cart-badge" id="cart-count">${cart_count}</span>
                                </#if>
                            </a>
                        <#else>
                            <a class="header-top-link nav-protected" href="#" title="Carrello">
                                <i class="ti ti-shopping-cart navbar-icon"></i>
                                <span class="header-top-label">Carrello</span>
                            </a>
                        </#if>

                    </div>

                </div>
            </div>

        </div>
    </nav>

    <!-- BREADCRUMB -->
    <#if breadcrumbs?? && breadcrumbs?has_content>
    <section class="section breadcrumb-section">
        <div class="container">
            <nav class="breadcrumb is-small" aria-label="breadcrumbs">
                <ul>
                    <#list breadcrumbs as crumb>
                        <#if crumb?is_last>
                            <li class="is-active">
                                <a href="#" aria-current="page">${crumb.label?html}</a>
                            </li>
                        <#else>
                            <li><a href="${crumb.url?html}">${crumb.label?html}</a></li>
                        </#if>
                    </#list>
                </ul>
            </nav>
        </div>
    </section>
    </#if>

    <!-- FLASH MESSAGE -->
    <#if flash_message??>
    <section class="section flash-section">
        <div class="container">
            <div class="notification is-${flash_type!'info'} is-light auto-hide">
                <button class="delete" aria-label="Chiudi"></button>
                ${flash_message?html}
            </div>
        </div>
    </section>
    </#if>

    <!-- CONTENUTO PRINCIPALE -->
    <main>
        <#nested/>
    </main>

    <!-- ========================================================= -->
    <!-- MODAL LOGIN GLOBALE -->
    <!-- ========================================================= -->
    <#if !utente??>
    <div class="login-modal" id="login-modal-nav" aria-hidden="true">
        <div class="modal-background"></div>
        <div class="login-modal-content">
            <button id="close-login-modal-nav" class="modal-close-btn" type="button" aria-label="Chiudi pop-up">&times;</button>
            <div class="login-modal-icon">
                <i class="ti ti-lock"></i>
            </div>
            <h3 class="login-modal-title">Accedi per continuare</h3>
            <p class="login-modal-text">
                Devi avere un account per accedere a questa sezione.
            </p>
            <div class="login-modal-actions">
                <a href="${base_url}/accedi" class="button btn-login-modal-accedi">
                    <i class="ti ti-login"></i> Accedi
                </a>
                <a href="${base_url}/registrati" class="button btn-login-modal-registrati">
                    Crea un account
                </a>
            </div>
        </div>
    </div>
    </#if>

    <!-- FOOTER -->
    <footer class="site-footer">
        <div class="container">
            <div class="columns">

                <div class="column is-3">
                    <h2 class="footer-heading">TABLECROWN</h2>
                    <p class="footer-desc">Il tuo negozio di giochi da tavolo.</p>
                    <p class="footer-desc">Prodotti e accessori di qualità.</p>
                    <div class="social-icons">
                        <a href="#" aria-label="Instagram"><i class="ti ti-brand-instagram"></i></a>
                        <a href="#" aria-label="Facebook"><i class="ti ti-brand-facebook"></i></a>
                        <a href="#" aria-label="Twitch"><i class="ti ti-brand-twitch"></i></a>
                    </div>
                </div>

                <div class="column is-3">
                    <h3 class="footer-heading">INFO</h3>
                    <ul class="footer-list">
                        <li><a href="${base_url}/chi-siamo">Chi siamo</a></li>
                        <li><a href="${base_url}/contatti">Contattaci</a></li>
                        <li><a href="${base_url}/dove-siamo">Dove siamo</a></li>
                    </ul>
                </div>

                <div class="column is-3">
                    <h3 class="footer-heading">ACCOUNT</h3>
                    <ul class="footer-list">
                        <li><a href="${base_url}/accedi">Accedi</a></li>
                        <li><a href="${base_url}/registrati">Registrati</a></li>
                    </ul>
                </div>

                <div class="column is-3">
                    <h3 class="footer-heading">CONTATTI</h3>
                    <ul class="footer-list footer-contacts">
                        <li>
                            <i class="ti ti-phone footer-contact-icon"></i>
                            <span>+39 344 253621</span>
                        </li>
                        <li>
                            <i class="ti ti-map-pin footer-contact-icon"></i>
                            <span>Giulianova, Abruzzo</span>
                        </li>
                    </ul>
                </div>

            </div>

            <div class="footer-divider"></div>

            <div class="footer-bottom">
                <p>© 2026 TableCrown — Tutti i diritti riservati</p>
            </div>
        </div>
    </footer>

    <!-- JS -->
    <script src="${base_url}/public/plugins/jQuery/jquery.min.js"></script>
    <script src="${base_url}/public/plugins/match-height/jquery.matchHeight-min.js"></script>
    <script src="${base_url}/public/js/script.js"></script>

    <script>
    document.addEventListener('DOMContentLoaded', function() {

        // DROPDOWN UTENTE
        var dropdown = document.getElementById('user-dropdown');
        if (dropdown) {
            dropdown.querySelector('.navbar-link').addEventListener('click', function(e) {
                e.stopPropagation();
                dropdown.classList.toggle('is-active');
            });
            document.addEventListener('click', function() {
                dropdown.classList.remove('is-active');
            });
        }

        // MODAL LOGIN NAVBAR
        var loginModalNav = document.getElementById('login-modal-nav');
        if (loginModalNav) {

            function apriLoginModalNav() {
                loginModalNav.classList.add('is-active');
                loginModalNav.setAttribute('aria-hidden', 'false');
                var btn = document.getElementById('close-login-modal-nav');
                if (btn) btn.focus();
            }

            function chiudiLoginModalNav() {
                loginModalNav.classList.remove('is-active');
                loginModalNav.setAttribute('aria-hidden', 'true');
            }

            var closeBtn = document.getElementById('close-login-modal-nav');
            if (closeBtn) {
                closeBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    chiudiLoginModalNav();
                });
            }

            var modalBg = loginModalNav.querySelector('.modal-background');
            if (modalBg) {
                modalBg.addEventListener('click', chiudiLoginModalNav);
            }

            loginModalNav.querySelectorAll('.login-modal-actions a').forEach(function(btn) {
                btn.addEventListener('click', function(e) { e.stopPropagation(); });
            });

            document.querySelectorAll('.nav-protected').forEach(function(link) {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    apriLoginModalNav();
                });
            });
        }
    });
    </script>

    <!-- JS Extra inserito dalle pagine figlie -->
    ${extra_js}

</body>
</html>
</#macro>