<#macro card p urlBase mostraAggiungiCarrello=false>
    <div class="product-card">
        <a href="${urlBase}/${p.idProdotto}" class="product-card-link">
            <#-- CONTENITORE IMMAGINE -->
            <div class="product-image-wrapper">

                <#-- Contenitore per i bollini in alto a sinistra -->
                <div class="product-badges-left">
                    <#-- Badge Disponibilità -->
                    <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                        <span class="product-badge product-badge-esaurito">Esaurito</span>
                    <#elseif p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                        <span class="product-badge product-badge-non-disponibile">Non disponibile</span>
                    </#if>

                    <#-- Badge Danno (appare sotto a quello di disponibilità se presenti entrambi) -->
                    <#if (p.livelloDanno)??>
                        <span class="product-badge product-badge-danno">
                            ${p.livelloDanno.name()?replace("_", " ")}
                        </span>
                    </#if>
                </div>

                <img src="${base_url}/public/img/prodotti/${p.imgProdotto!'placeholder.png'}"
                     alt="${p.nomeProdotto?html}"
                     class="product-image">
            </div>

            <#-- CONTENUTO SCHEDA -->
            <div class="product-info">
                <h3 class="product-name">${p.nomeProdotto?html}</h3>

                <#-- PREZZO DEPENNATO E SCONTO -->
                <div class="product-price-wrapper">
                    <#if (p.prezzoScontato < p.prezzo)>
                        <span class="product-price">${p.prezzoScontato?string("0.00")} €</span>
                        <span class="product-price-old">${p.prezzo?string("0.00")} €</span>
                    <#else>
                        <span class="product-price">${p.prezzo?string("0.00")} €</span>
                    </#if>
                </div>
            </div>
        </a>

        <#if mostraAggiungiCarrello>
            <form action="${base_url}/carrello/aggiungi" method="POST" class="card-add-cart-form">
                <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                <input type="hidden" name="quantita" value="1">

                <#if p.isDisponibile()>
                    <button type="submit" class="btn-add-cart">
                        <i class="ti ti-shopping-cart"></i> Aggiungi al Carrello
                    </button>
                <#else>
                    <#assign testoPulsante = "Non disponibile">
                    <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                        <#assign testoPulsante = "Esaurito">
                    </#if>
                    <button type="button" class="btn-add-cart" disabled >
                        <i class="ti ti-ban"></i> ${testoPulsante}
                    </button>
                </#if>
            </form>
        </#if>
    </div>
</#macro>