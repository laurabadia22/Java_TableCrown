<#macro card p urlBase mostraAggiungiCarrello=false>
    <div class="card home-card-fixed prodotto-card">
        <a href="${urlBase}/${p.idProdotto}" class="card-link-wrapper">
            <div class="card-image">
                <figure class="image-container-fixed">
                    <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                        <span class="badge-stato badge-esaurito">Esaurito</span>
                    <#elseif p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                        <span class="badge-stato badge-non-disponibile">Non disponibile</span>
                    </#if>
                    <img src="${base_url}/public/img/prodotti/${p.imgProdotto!'placeholder.png'}"
                         alt="${p.nomeProdotto?html}">
                </figure>
            </div>
            <div class="card-content">
                <p class="card-title-custom">${p.nomeProdotto?html}</p>
                <div class="price-container">
                    <#if p.prezzoScontato?? && p.prezzoScontato < p.prezzo>
                        <span class="price">${p.prezzoScontato?string("0.00")} €</span>
                        <span class="price-old">${p.prezzo?string("0.00")} €</span>
                    <#else>
                        <span class="price">${p.prezzo?string("0.00")} €</span>
                    </#if>
                </div>
            </div>
        </a>

        <#if mostraAggiungiCarrello>
            <form action="${base_url}/carrello/aggiungi" method="POST" class="card-add-cart-form">
                <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                <input type="hidden" name="quantita" value="1">
                <button type="submit" class="btn-cart">
                    <i class="ti ti-shopping-cart"></i> Aggiungi
                </button>
            </form>
        </#if>
    </div>
</#macro>