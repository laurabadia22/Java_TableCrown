<#import "layout.ftl" as layout>
<#import "prodottoCard.ftl" as pc>
<#import "paginazione.ftl" as pag>
<#import "ricerca.ftl" as r>

<#macro renderCatalogoBase titolo subpage urlBase prodotti filtri paginaCorrente totalePagine query breadcrumbs extraFiltriMacro="">
    <#assign cssCatalogo>
        <link rel="stylesheet" href="${base_url}/public/css/catalogo.css">
        <link rel="stylesheet" href="${base_url}/public/css/home.css">
    </#assign>

    <@layout.page
    page_title="${titolo} - TableCrown"
    current_page="catalogo"
    current_subpage=subpage
    breadcrumbs=breadcrumbs
    extra_css=cssCatalogo>

        <div class="container section px-4">
            <h1 class="title section-title mb-5">${titolo}</h1>

            <#-- BARRA DI RICERCA -->
            <div class="mb-4">
                <@r.barraRicerca actionUrl=urlBase query=query!"" />
            </div>

            <#-- BOX FILTRI -->
            <div class="box catalog-filters-box mb-6" style="background-color: var(--color-bg-dark-2); border: 1px solid var(--color-border-dark);">
                <form method="get" action="${urlBase}">
                    <div class="columns is-multiline align-items-center">

                        <#-- Prezzo Minimo -->
                        <div class="column is-6-mobile is-3-tablet is-2-desktop">
                            <div class="field">
                                <label class="label has-text-light is-size-7">Prezzo Min</label>
                                <div class="control">
                                    <input class="input" type="number" step="0.01" name="prezzoMin" value="${filtri.prezzoMin!''}" placeholder="€ Min">
                                </div>
                            </div>
                        </div>

                        <#-- Prezzo Massimo -->
                        <div class="column is-6-mobile is-3-tablet is-2-desktop">
                            <div class="field">
                                <label class="label has-text-light is-size-7">Prezzo Max</label>
                                <div class="control">
                                    <input class="input" type="number" step="0.01" name="prezzoMax" value="${filtri.prezzoMax!''}" placeholder="€ Max">
                                </div>
                            </div>
                        </div>

                        <#-- Filtri extra specifici per categoria (se passati) -->
                        <#if extraFiltriMacro?is_macro>
                            <@extraFiltriMacro />
                        </#if>

                        <#-- Bottone Submit -->
                        <div class="column is-12-mobile is-3-tablet is-2-desktop" style="margin-top: auto;">
                            <div class="field">
                                <button class="button is-primary is-fullwidth font-weight-bold" type="submit">
                                    <i class="ti ti-filter mr-1"></i> Filtra
                                </button>
                            </div>
                        </div>

                    </div>
                </form>
            </div>

            <#-- GRIGLIA PRODOTTI (3 colonne per riga su Desktop) -->
            <#if prodotti?? && (prodotti?size > 0)>
                <div class="columns is-multiline">
                    <#list prodotti as p>
                        <div class="column is-12-mobile is-6-tablet is-4-desktop flex-card-column">
                            <@pc.card p=p urlBase=urlBase mostraAggiungiCarrello=true />
                        </div>
                    </#list>
                </div>
            <#else>
                <div class="notification is-warning is-light text-center my-6">
                    <i class="ti ti-alert-circle mr-1"></i> Nessun prodotto trovato con i filtri selezionati.
                </div>
            </#if>

            <#-- PAGINAZIONE -->
            <div class="mt-6">
                <@pag.paginazione paginaCorrente=paginaCorrente!1 totalePagine=totalePagine!1 baseUrl=urlBase />
            </div>
        </div>
    </@layout.page>
</#macro>

<#-- Macro di fallback per i cataloghi semplici senza filtri aggiuntivi -->
<#macro renderCatalogoSemplice titolo subpage urlBase prodotti filtri paginaCorrente totalePagine query breadcrumbs>
    <@renderCatalogoBase
    titolo=titolo
    subpage=subpage
    urlBase=urlBase
    prodotti=prodotti
    filtri=filtri
    paginaCorrente=paginaCorrente
    totalePagine=totalePagine
    query=query
    breadcrumbs=breadcrumbs />
</#macro>