<#import "common/layout.ftl" as layout>

<#assign cssInfo>
    <link rel="stylesheet" href="${base_url}/public/css/pagineStatiche.css">
</#assign>

<@layout.page page_title="Chi Siamo - TableCrown" current_page=currentPage!'chi_siamo' extra_css=cssInfo>
    <div class="container section px-4">

        <h1 class="title section-title mb-6">Chi Siamo</h1>

        <div class="columns is-vcentered my-5">
            <div class="column is-6">
                <img src="${base_url}/public/img/chi-siamo-store.jpg" alt="Il nostro negozio" class="info-image">
            </div>
            <div class="column is-6 info-text-content">
                <h2 class="title is-4 mb-4">La nostra Passione per il Gioco</h2>
                <p class="mb-4">
                    Nata nel 2024, TableCrown è il punto di riferimento per gli appassionati di giochi da tavolo,
                    carte collezionabili e accessori. Crediamo che il gioco non sia solo un passatempo, ma un modo
                    per creare connessioni autentiche tra le persone.
                </p>
                <p>
                    Selezioniamo con cura ogni singolo prodotto, dai grandi classici alle novità più esclusive,
                    per garantire ai nostri clienti sempre il meglio del mercato ludico.
                </p>
            </div>
        </div>

    </div>
</@layout.page>