<#import "common/layout.ftl" as layout>

<#-- Colleghiamo un file CSS esterno per le personalizzazioni del gestore senza iniettare stili -->
<#assign dashboard_css>
    <link rel="stylesheet" href="${base_url}/public/css/dashboard_gestore.css">
    <link rel="stylesheet" href="${base_url}/public/css/home.css">
</#assign>

<#-- Se in futuro servirà JS specifico per la dashboard, lo colleghiamo qui -->
<#--<#assign dashboard_js>-->
<#--    <script src="${base_url}/public/js/dashboard_gestore.js" defer></script>-->
<#--</#assign>-->

<@layout.page page_title="Dashboard Gestore - TableCrown" extra_css=dashboard_css extra_js=dashboard_js current_page="gestore_dashboard">

    <div class="container px-4 mt-6 mb-6">

        <!-- HEADER DASHBOARD -->
        <div class="mb-5">
            <h1 class="title is-2">Area Gestore</h1>
            <p class="subtitle is-5 has-text-grey">Panoramica delle attività e gestione del negozio.</p>
        </div>

        <!-- STATISTICHE PRINCIPALI (Ordini e Vendite) -->
        <div class="columns is-multiline">

            <div class="column is-6-tablet is-4-desktop">
                <div class="card">
                    <div class="card-content has-text-centered py-6">
                        <i class="ti ti-shopping-cart is-size-1 has-text-link mb-3"></i>
                        <p class="heading is-size-6">Ordini Totali Ricevuti</p>
                        <p class="title is-2">${ordiniTotali!'0'}</p>
                        <div class="mt-4">
                            <a href="${base_url}/gestore/ordini" class="button is-small is-link is-light is-rounded">Gestisci ordini</a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="column is-6-tablet is-4-desktop">
                <div class="card">
                    <div class="card-content has-text-centered py-6">
                        <i class="ti ti-currency-euro is-size-1 has-text-success mb-3"></i>
                        <p class="heading is-size-6">Incasso Totale</p>
                        <p class="title is-2">
                            <#if venditeTotali??>
                                € ${venditeTotali?string("0.00")}
                            <#else>
                                € 0.00
                            </#if>
                        </p>
                        <div class="mt-4">
                            <span class="tag is-success is-light is-medium">Aggiornato a oggi</span>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- AZIONI RAPIDE (Aggiunta Prodotti) -->
        <div class="mt-6">
            <h2 class="title is-4 mb-4">Azioni Rapide al Catalogo</h2>

            <div class="columns is-multiline">
                <div class="column is-12-tablet is-8-desktop">
                    <div class="box px-5 py-5">
                        <p class="subtitle is-6 mb-4">Seleziona la tipologia di prodotto che desideri aggiungere al catalogo:</p>

                        <div class="buttons">
                            <a href="${base_url}/gestore/crea/gioco-da-tavolo" class="button is-primary is-medium">
                                <span class="icon"><i class="ti ti-dice-5"></i></span>
                                <span>Nuovo Gioco</span>
                            </a>

                            <a href="${base_url}/gestore/crea/bustine" class="button is-info is-medium">
                                <span class="icon"><i class="ti ti-cards"></i></span>
                                <span>Nuova Bustina</span>
                            </a>

                            <a href="${base_url}/gestore/crea/porta-dadi" class="button is-warning is-medium">
                                <span class="icon"><i class="ti ti-package"></i></span>
                                <span>Nuovo Portadadi</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

</@layout.page>