<#import "common/layout.ftl" as layout>
<#import "common/prodottoRiga.ftl" as righe>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/prodottoRiga.css">
    <link rel="stylesheet" href="${base_url}/public/css/wishlist.css">
</#assign>

<@layout.page page_title="La mia Wishlist - TableCrown" extra_css=extra_css extra_js="">

    <div class="wishlist-container">
        <div class="container">

            <div class="wishlist-topbar">
                <a href="${base_url}/profilo" class="wishlist-back-link">
                    <i class="ti ti-arrow-left"></i> Torna all'Area Personale
                </a>
            </div>

            <div class="wishlist-header">
                <div class="wishlist-header-text">
                    <span class="wishlist-eyebrow">Area Personale</span>
                    <h1 class="wishlist-titolo">
                        <i class="ti ti-heart-filled"></i> La Mia Wishlist
                    </h1>
                </div>

                <#if prodotti?has_content>
                    <div class="wishlist-count-badge">
                        <span class="wishlist-count-num">${prodotti?size}</span>
                        <span class="wishlist-count-label"><#if prodotti?size == 1>articolo<#else>articoli</#if></span>
                    </div>
                </#if>
            </div>

            <#if prodotti?has_content>
                <div class="wishlist-grid" id="wishlist-grid">
                    <#list prodotti as p>
                        <@righe.riga p=p
                        mostraRimuovi=true
                        azioneRimuovi="${base_url}/wishlist/rimuovi"
                        iconaRimuovi="ti-heart-filled"
                        mostraAggiungiCarrello=true />
                    </#list>
                </div>
            <#else>
                <div class="wishlist-empty" id="wishlist-empty">
                    <div class="wishlist-empty-icon">
                        <i class="ti ti-heart"></i>
                    </div>
                    <h2 class="wishlist-empty-titolo">La tua wishlist è vuota</h2>
                    <p class="wishlist-empty-testo">Salva i prodotti che ti piacciono per ritrovarli facilmente quando vuoi.</p>
                    <a href="${base_url}/catalogo/giochi-da-tavolo" class="wishlist-empty-btn">
                        <i class="ti ti-shopping-bag"></i> Scopri il catalogo
                    </a>
                </div>
            </#if>

        </div>
    </div>

</@layout.page>