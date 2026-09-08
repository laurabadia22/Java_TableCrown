<#import "common/layout.ftl" as layout>
<#import "common/prodottoCardVector.ftl" as cardVector>

<#-- Script per il carosello iniettato in extra_js -->

<#assign home_css>
    <link rel="stylesheet" href="${base_url}/public/css/home.css">
</#assign>

<#assign carosello_script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            let currentSlide = 0;
            const items = document.querySelectorAll('#carousel-inner .carousel-item');
            const totalSlides = items.length; // Prende il totale dinamicamente
            const inner = document.getElementById('carousel-inner');

            function moveSlide(index) {
                currentSlide = (index + totalSlides) % totalSlides;
                //Sposta la percentuale corretta in base al numero totale delle slides
                let percentage = currentSlide * (100 / totalSlides);
                inner.style.transform = 'translateX(-' + percentage + '%)';
            }

            const nextBtn = document.getElementById('next-slide');
            const prevBtn = document.getElementById('prev-slide');

            if (nextBtn) {
                nextBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    moveSlide(currentSlide + 1);
                });
            }

            if (prevBtn) {
                prevBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    moveSlide(currentSlide - 1);
                });
            }

            //Cambio automatico ogni 5 secondi
            setInterval(function() {
                moveSlide(currentSlide + 1);
                }, 5000);
        });
    </script>
</#assign>

<@layout.page page_title="Home - TableCrown" extra_css=home_css extra_js=carosello_script>

    <div class="container px-4">

        <!-- SEARCH BAR -->
        <div class="home-search-bar">
            <form class="home-search-form" action="${base_url}/catalogo/giochi-da-tavolo" method="get">
                <input class="input home-search-input" type="search" name="q" placeholder="Cerca tra i Giochi..." value="${(RequestParameters.q)!''}" aria-label="Cerca nel catalogo">
                <button class="button home-search-btn" type="submit" aria-label="Cerca">
                    <i class="ti ti-search"></i>
                </button>
            </form>
        </div>

        <!-- CAROSELLO IMMAGINI -->
        <div class="hero-carousel" id="home-carousel">
            <div class="carousel-inner" id="carousel-inner">
                <div class="carousel-item">
                    <img src="${base_url}/public/img/carousel/carousel_slide1.jpg" alt="Nuovi Giochi">
                    <div class="carousel-caption">
                        <h2 class="title is-3 has-text-white">Esplora le ultime novità</h2>
                        <p class="subtitle is-5 has-text-warning">I migliori titoli del 2026 arrivano su TableCrown</p>
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${base_url}/public/img/carousel/carousel_slide2.jpg" alt="Bustine e Accessori">
                    <div class="carousel-caption">
                        <h2 class="title is-3 has-text-white">Proteggi la tua collezione</h2>
                        <p class="subtitle is-5 has-text-warning">Bustine protettive e porta dadi per le tue partite</p>
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${base_url}/public/img/carousel/carousel_slide3.jpg" alt="Offerte Speciali">
                    <div class="carousel-caption">
                        <h2 class="title is-3 has-text-white">Sconti imbattibili</h2>
                        <p class="subtitle is-5 has-text-warning">Fino al 40% di sconto sui giochi di strategia</p>
                    </div>
                </div>
            </div>
            <div class="carousel-nav">
                <button class="button is-rounded" id="prev-slide"><i class="ti ti-chevron-left"></i></button>
                <button class="button is-rounded" id="next-slide"><i class="ti ti-chevron-right"></i></button>
            </div>
        </div>

        <!-- ZONA: OFFERTE IN SCADENZA -->
        <section class="home-section">
            <h2 class="title section-title is-4 text-uppercase">🔥 Offerte in Scadenza</h2>
            <div class="card-row-vector">

                <#if offerte?? && offerte?has_content>
                    <#list offerte as prodotto>
                        <@cardVector.card p=prodotto />
                    </#list>

                    <div class="card-vector-item card-vector-more">
                        <a href="${base_url}/offerte" class="view-more-link" title="Vedi tutte le offerte">
                            <div class="circle-plus"><span>+</span></div>
                            <span class="view-more-text">Vedi tutti</span>
                        </a>
                    </div>
                <#else>
                    <p class="empty-section-message">Nessuna offerta disponibile al momento.</p>
                </#if>
            </div>
        </section>

    </div>

</@layout.page>