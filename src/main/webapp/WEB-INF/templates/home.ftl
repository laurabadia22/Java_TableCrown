<#import "common/layout.ftl" as layout>

<#-- Script per il carosello iniettato in extra_js -->
<#assign carosello_script>
    <script>
        $(document).ready(function() {
            let currentSlide = 0;
            const totalSlides = 3;
            const $inner = $('#carousel-inner');

            function moveSlide(index) {
                currentSlide = (index + totalSlides) % totalSlides;
                $inner.css('transform', 'translateX(-' + (currentSlide * 100 / totalSlides) + '%)');
            }

            $('#next-slide').click(function() { moveSlide(currentSlide + 1); });
            $('#prev-slide').click(function() { moveSlide(currentSlide - 1); });
            setInterval(function() { moveSlide(currentSlide + 1); }, 5000);
        });
    </script>
</#assign>

<@layout.page page_title="Home - TableCrown" extra_js=carosello_script>

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
                    <img src="${base_url}/public/img/carousel/slide1.jpg" alt="Nuovi Giochi">
                    <div class="carousel-caption">
                        <h2 class="title is-3 has-text-white">Esplora le ultime novità</h2>
                        <p class="subtitle is-5 has-text-warning">I migliori titoli del 2026 arrivano su TableCrown</p>
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${base_url}/public/img/carousel/slide2.jpg" alt="Bustine e Accessori">
                    <div class="carousel-caption">
                        <h2 class="title is-3 has-text-white">Proteggi la tua collezione</h2>
                        <p class="subtitle is-5 has-text-warning">Bustine protettive e porta dadi per le tue partite</p>
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${base_url}/public/img/carousel/slide3.jpg" alt="Offerte Speciali">
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
                        <div class="card-vector-item">
                            <a href="${base_url}/prodotto?id=${prodotto.idProdotto}" class="card-link-wrapper">
                                <div class="card home-card-fixed">
                                    <div class="card-image">
                                        <figure class="image-container-fixed">

                                            <!-- Badge Disponibilità -->
                                            <#if prodotto.disponibilitaProdotto?? && prodotto.disponibilitaProdotto.name() == 'ESAURITO'>
                                                <span class="badge-stato badge-esaurito">Esaurito</span>
                                            <#elseif prodotto.disponibilitaProdotto?? && prodotto.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                                                <span class="badge-stato badge-non-disponibile">Non disponibile</span>
                                            </#if>

                                            <!-- Logica Immagini -->
                                            <#assign imgUrl = base_url + "/public/img/placeholder.png">
                                            <#if prodotto.imgProdotto?? && prodotto.imgProdotto != "">
                                                <#assign imgUrl = base_url + "/public/img/prodotti/" + prodotto.imgProdotto>
                                            </#if>
                                            <img src="${imgUrl}" alt="${prodotto.nomeProdotto}" />
                                        </figure>
                                    </div>
                                    <div class="card-content">
                                        <p class="card-title-custom">${prodotto.nomeProdotto}</p>

                                        <!-- Prezzi formattati -->
                                        <div class="price-container">
                                            <#if prodotto.sconto.hasSconto()>
                                                <#assign prezzoScontato = prodotto.prezzo * (1 - prodotto.sconto.sconto / 100)>
                                                <span class="price">${prezzoScontato?string("0.00")} €</span>
                                                <span class="price-old">${prodotto.prezzo?string("0.00")} €</span>
                                            <#else>
                                                <span class="price">${prodotto.prezzo?string("0.00")} €</span>
                                            </#if>
                                        </div>

                                        <!-- Form aggiunta al carrello -->
                                        <form action="${base_url}/carrello/aggiungi" method="POST" style="margin-top: 10px;">
                                            <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                            <input type="hidden" name="quantita" value="1">
                                            <button type="submit" class="btn-cart">
                                                <i class="ti ti-shopping-cart"></i> Aggiungi
                                            </button>
                                        </form>

                                    </div>
                                </div>
                            </a>
                        </div>
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