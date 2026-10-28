<#import "common/layout.ftl" as layout>

<#assign cssInfo>
    <link rel="stylesheet" href="${base_url}/public/css/pagineStatiche.css">
</#assign>

<@layout.page page_title="Dove Siamo - TableCrown" current_page="dove_siamo" extra_css=cssInfo>
    <div class="container section px-4">

        <h1 class="title section-title mb-6">Vieni a Trovarci</h1>

        <div class="columns is-variable is-6 my-5 is-align-items-stretch">

            <#-- Colonna Contatti e Orari -->
            <div class="column is-4">
                <div class="info-card">
                    <h3 class="title is-4 info-card-title mb-4">
                        <i class="ti ti-map-pin"></i> Indirizzo
                    </h3>
                    <p class="info-card-text mb-5">
                        Via dei Giochi da Tavolo, 42<br>
                        67100 - L'Aquila (AQ)<br>
                        Italia
                    </p>

                    <h3 class="title is-4 info-card-title mb-4">
                        <i class="ti ti-clock"></i> Orari di Apertura
                    </h3>
                    <ul class="info-card-list mb-5">
                        <li><strong>Lun - Ven:</strong> <span>09:00 - 13:00 / 15:30 - 19:30</span></li>
                        <li><strong>Sabato:</strong> <span>10:00 - 19:30 (Orario Continuato)</span></li>
                        <li><strong>Domenica:</strong> <span>Chiuso</span></li>
                    </ul>

                    <h3 class="title is-4 info-card-title mb-4">
                        <i class="ti ti-phone"></i> Contatti
                    </h3>
                    <p class="info-card-text">
                        <i class="ti ti-mail"></i> info@tablecrown.it<br>
                        <i class="ti ti-phone-call"></i> +39 0862 123456
                    </p>
                </div>
            </div>

            <#-- Colonna Google Maps -->
            <div class="column is-8">
                <div class="map-wrapper">
                    <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2944.5!2d13.399!3d42.349!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zNDLCsDIwJzU2LjQiTiAxM8KwMjMnNTYuNCJF!5e0!3m2!1sit!2sit!4v1600000000000!5m2!1sit!2sit"
                            class="map-iframe"
                            allowfullscreen=""
                            loading="lazy"
                            title="Mappa di TableCrown">
                    </iframe>
                </div>
            </div>

        </div>
    </div>
</@layout.page>