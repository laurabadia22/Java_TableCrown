<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/areaPersonale.css">
</#assign>

<@layout.page page_title="Area Personale - TableCrown" extra_css=extra_css extra_js="">

    <div class="account-container">
        <div class="container">

            <div class="account-layout">

                <div class="account-main">

                    <div class="account-header">

                        <div class="account-avatar">
                            <#if datiHub.immagineUtente?has_content>
                                <img src="${base_url}/${datiHub.immagineUtente}"                                     onerror="this.onerror=null; this.src='${base_url}/public/img/avatar-default.png'"
                                     alt="${datiHub.nomeUtente?html}"
                                     class="account-avatar-img">
                            <#else>
                                <i class="ti ti-user"></i>
                            </#if>
                        </div>

                        <div class="account-welcome">
                            <span class="account-eyebrow">Area Personale</span>
                            <h1 class="account-titolo">Ciao, ${datiHub.nomeUtente?html}!</h1>

                            <div class="account-header-links">
                                <a href="${base_url}/logout" class="account-link-secondary">
                                    <i class="ti ti-logout"></i> Logout
                                </a>
                            </div>
                        </div>

                    </div>

                    <div class="account-grid">
                        <#list datiHub.menuVoci as voce>
                            <a href="${base_url}${voce.url}" class="account-card">
                                <span class="account-card-icon">
                                    <i class="ti ti-chevron-right"></i>
                                </span>
                                <span class="account-card-label">${voce.label?html}</span>
                                <span class="account-card-arrow">
                                    <i class="ti ti-arrow-right"></i>
                                </span>
                            </a>
                        </#list>
                    </div>

                </div>

            </div>

        </div>
    </div>

</@layout.page>