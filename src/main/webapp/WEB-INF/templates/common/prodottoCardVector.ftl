<#macro card p mostraRating=true>
    <div class="card-vector-item">
        <div class="card home-card-fixed">

            <#-- Immagine con badge -->
            <a href="${base_url}/prodotto?id=${p.idProdotto?c}" class="card-link-wrapper">
                <div class="card-image">
                    <figure class="image-container-fixed">
                        <#-- Badge Disponibilità -->
                        <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                            <span class="badge-stato badge-esaurito">Esaurito</span>
                        <#elseif p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                            <span class="badge-stato badge-non-disponibile">Non disponibile</span>
                        </#if>

                        <#-- Immagine Prodotto -->
                        <#assign imgUrl = base_url + "/public/img/placeholder.png">
                        <#if p.imgProdotto?? && p.imgProdotto != "">
                            <#assign imgUrl = base_url + "/public/img/prodotti/" + p.imgProdotto>
                        </#if>
                        <img src="${imgUrl}" alt="${(p.nomeProdotto)?html}" />
                    </figure>
                </div>
            </a>

            <#-- Dettagli e Azioni -->
            <div class="card-content">
                <a href="${base_url}/prodotto?id=${p.idProdotto?c}" class="card-title-link">
                    <p class="card-title-custom">${(p.nomeProdotto)?html}</p>
                </a>

                <#-- Rating Stelle -->
                <#if mostraRating>
                    <#assign cMedia = (p.valutazioneMedia)!0>
                    <div class="card-rating" style="color: #f59e0b; font-size: 0.85rem; margin-bottom: 0.35rem;">
                        <#list 1..5 as s>
                            <#if s <= cMedia>
                                <i class="ti ti-star-filled"></i>
                            <#elseif (s - cMedia) < 1>
                                <i class="ti ti-star-half-filled"></i>
                            <#else>
                                <i class="ti ti-star"></i>
                            </#if>
                        </#list>
                    </div>
                </#if>

                <#-- Prezzo -->
                <div class="price-container">
                    <#if p.prezzoScontato?? && (p.prezzoScontato < p.prezzo)>
                        <span class="price">€${p.prezzoScontato?string("0.00")}</span>
                        <span class="price-old">€${p.prezzo?string("0.00")}</span>
                    <#elseif p.sconto?? && p.sconto.hasSconto()>
                        <#assign prezzoScontato = p.prezzo * (1 - p.sconto.sconto / 100)>
                        <span class="price">€${prezzoScontato?string("0.00")}</span>
                        <span class="price-old">€${p.prezzo?string("0.00")}</span>
                    <#else>
                        <span class="price">€${p.prezzo?string("0.00")}</span>
                    </#if>
                </div>

                <#-- Form Reale per aggiunta al Carrello -->
                <form action="${base_url}/carrello/aggiungi" method="POST" style="margin-top: 10px;">
                    <input type="hidden" name="idProdotto" value="${p.idProdotto?c}">
                    <input type="hidden" name="quantita" value="1">
                    <#if (p.isAcquistabile())!true>
                        <button type="submit" class="btn-cart">
                            <i class="ti ti-shopping-cart"></i> Aggiungi
                        </button>
                    <#else>
                        <button type="button" class="btn-cart" disabled style="opacity: 0.6; cursor: not-allowed;">
                            <i class="ti ti-ban"></i> Non disponibile
                        </button>
                    </#if>
                </form>

            </div>

        </div>
    </div>
</#macro>