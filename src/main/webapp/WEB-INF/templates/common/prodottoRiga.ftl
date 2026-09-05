<#macro riga p mostraRimuovi=false azioneRimuovi="" iconaRimuovi="ti-trash" mostraAggiungiCarrello=false mostraQuantita=false quantita=1>
    <div class="prodotto-riga <#if !p.isAcquistabile()>prodotto-riga-esaurito</#if>">

        <a href="${base_url}/prodotto?id=${p.idProdotto}" class="prodotto-riga-media">
            <img src="${base_url}/public/img/prodotti/${p.imgProdotto!'placeholder.png'}"
                 alt="${p.nomeProdotto?html}"
                 class="prodotto-riga-img">

            <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                <span class="prodotto-riga-badge prodotto-riga-badge-esaurito">Esaurito</span>
            <#elseif p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                <span class="prodotto-riga-badge prodotto-riga-badge-esaurito">Non disponibile</span>
            </#if>
        </a>

        <div class="prodotto-riga-info">
            <a href="${base_url}/prodotto?id=${p.idProdotto}" class="prodotto-riga-nome">
                ${p.nomeProdotto?html}
            </a>

            <div class="prodotto-riga-prezzo-row">
                <#if p.prezzoScontato < p.prezzo>
                    <span class="prodotto-riga-prezzo">${p.prezzoScontato?string("0.00")} €</span>
                    <span class="prodotto-riga-prezzo-old">${p.prezzo?string("0.00")} €</span>
                <#else>
                    <span class="prodotto-riga-prezzo">${p.prezzo?string("0.00")} €</span>
                </#if>
            </div>
        </div>

        <#if mostraQuantita>
            <div class="prodotto-riga-quantita">
                <span class="prodotto-riga-quantita-label">Qtà</span>
                <span class="prodotto-riga-quantita-val">${quantita}</span>
            </div>
        </#if>

        <div class="prodotto-riga-azioni">
            <#if mostraAggiungiCarrello>
                <form action="${base_url}/carrello/aggiungi" method="POST" class="prodotto-riga-form">
                    <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                    <input type="hidden" name="quantita" value="1">
                    <#if p.isAcquistabile()>
                        <button type="submit" class="prodotto-riga-btn-carrello">
                            <i class="ti ti-shopping-cart-plus"></i> Aggiungi al carrello
                        </button>
                    <#else>
                        <button type="button" class="prodotto-riga-btn-carrello" disabled>
                            <i class="ti ti-ban"></i> Non disponibile
                        </button>
                    </#if>
                </form>
            </#if>

            <#if mostraRimuovi>
                <form action="${azioneRimuovi}" method="POST" class="prodotto-riga-form">
                    <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                    <button type="submit" class="prodotto-riga-btn-rimuovi" aria-label="Rimuovi" title="Rimuovi">
                        <i class="ti ${iconaRimuovi}"></i>
                    </button>
                </form>
            </#if>
        </div>

    </div>
</#macro>