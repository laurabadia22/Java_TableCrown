<#import "layout.ftl" as layout>
<#import "prodottoCardGestore.ftl" as pcg>
<#import "paginazione.ftl" as pag>
<#import "ricerca.ftl" as r>

<#macro renderCatalogoGestore titolo subpage urlBase urlNuovo prodotti paginaCorrente totalePagine query breadcrumbs>
    <#assign cssCatalogo>
        <link rel="stylesheet" href="${base_url}/public/css/catalogo.css">
        <link rel="stylesheet" href="${base_url}/public/css/home.css">
    </#assign>

    <#assign qParams = "">
    <#if query?? && query?has_content>
        <#assign qParams = "&q=" + query?url('UTF-8')>
    </#if>

    <@layout.page page_title="${titolo} - Gestore" current_page="gestore_${subpage}" breadcrumbs=breadcrumbs extra_css=cssCatalogo>

        <div class="container section px-4">

            <#-- HEADER CON TASTO CREA -->
            <div class="is-flex is-justify-content-space-between is-align-items-center mb-5 is-flex-wrap-wrap">
                <h1 class="title section-title m-0">${titolo}</h1>
                <a href="${base_url}${urlNuovo}" class="button is-primary font-weight-bold mt-2">
                    <i class="ti ti-plus mr-2"></i> Nuovo Prodotto
                </a>
            </div>

            <#-- BARRA DI RICERCA -->
            <div class="mb-5">
                <@r.barraRicerca actionUrl=urlBase query=query!"" />
            </div>

            <#-- GRIGLIA PRODOTTI -->
            <#if prodotti?? && (prodotti?size > 0)>
                <div class="columns is-multiline">
                    <#list prodotti as p>
                        <div class="column is-12-mobile is-6-tablet is-4-desktop flex-card-column">
                            <@pcg.cardGestore p=p />
                        </div>
                    </#list>
                </div>
            <#else>
                <div class="notification is-warning is-light text-center my-6">
                    <i class="ti ti-package-off mr-1"></i> Nessun prodotto trovato nel catalogo.
                </div>
            </#if>

            <#-- PAGINAZIONE -->
            <div class="mt-6">
                <@pag.paginazione paginaCorrente=paginaCorrente!1 totalePagine=totalePagine!1 baseUrl=urlBase queryParams=qParams/>
            </div>

        </div>
    </@layout.page>
</#macro>