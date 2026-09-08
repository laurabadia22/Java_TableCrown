<#--
    Macro riga: riga prodotto riusabile tra wishlist e carrello.

    Modalità wishlist:
        mostraAggiungiCarrello=true, quantitaEditabile=false (default)
        -> bottone "Aggiungi al carrello" + bottone rimuovi a icona singola

    Modalità carrello:
        quantitaEditabile=true, mostraRimuovi=true
        -> controlli quantità +/- e bottone "Rimuovi" dentro un unico <form>,
           esattamente come nel vecchio carrello.ftl (il bottone rimuovi
           sovrascrive l'azione del form con formaction, niente JS).
           Il prezzo mostrato è quello bloccato sull'item (prezzoUnitario),
           non p.prezzoScontato, perché nel carrello il prezzo è quello
           "congelato" al momento dell'aggiunta.
-->
<#macro riga p
mostraRimuovi=false
azioneRimuovi=""
iconaRimuovi="ti-trash"
mostraAggiungiCarrello=false
mostraQuantita=false
quantitaEditabile=false
azioneQuantita=""
quantita=1
prezzoUnitario=0
mostraSubtotale=false
subtotale=0>

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
                <#if quantitaEditabile>
                <#-- carrello: prezzo bloccato sull'item, non ricalcolato da p -->
                    <#if p.sconto.hasSconto()>
                        <span class="prodotto-riga-prezzo">€${prezzoUnitario?string("0.00")}</span>
                        <span class="prodotto-riga-prezzo-old">€${p.prezzo?string("0.00")}</span>
                        <span class="prodotto-riga-sconto-badge">-${p.sconto.sconto?round}%</span>
                    <#else>
                        <span class="prodotto-riga-prezzo">€${prezzoUnitario?string("0.00")}</span>
                    </#if>
                <#else>
                    <#if p.prezzoScontato < p.prezzo>
                        <span class="prodotto-riga-prezzo">${p.prezzoScontato?string("0.00")} €</span>
                        <span class="prodotto-riga-prezzo-old">${p.prezzo?string("0.00")} €</span>
                    <#else>
                        <span class="prodotto-riga-prezzo">${p.prezzo?string("0.00")} €</span>
                    </#if>
                </#if>
            </div>
        </div>

        <#if quantitaEditabile>
            <form action="${azioneQuantita}" method="post" class="prodotto-riga-form prodotto-riga-quantita-form">
                <input type="hidden" name="idProdotto" value="${p.idProdotto}">

                <div class="prodotto-riga-quantita prodotto-riga-quantita-editabile">
                    <button class="button quantita-btn"
                            type="submit"
                            name="quantita"
                            value="${quantita - 1}"
                            <#if (quantita <= 1)>disabled</#if>
                            aria-label="Riduci quantità">
                        <i class="ti ti-minus"></i>
                    </button>

                    <span class="prodotto-riga-quantita-val">${quantita}</span>

                    <button class="button quantita-btn"
                            type="submit"
                            name="quantita"
                            value="${quantita + 1}"
                            <#if (quantita >= p.quantita)>disabled</#if>
                            aria-label="Aumenta quantità">
                        <i class="ti ti-plus"></i>
                    </button>
                </div>

                <#if mostraSubtotale>
                    <div class="prodotto-riga-subtotale">
                        <span class="prodotto-riga-subtotale-label">Subtotale</span>
                        <span class="prodotto-riga-subtotale-value">€${subtotale?string("0.00")}</span>
                    </div>
                </#if>

                <#if mostraRimuovi>
                    <button class="prodotto-riga-btn-rimuovi prodotto-riga-btn-rimuovi-testo"
                            type="submit"
                            formaction="${azioneRimuovi}"
                            aria-label="Rimuovi ${p.nomeProdotto?html} dal carrello">
                        <i class="ti ${iconaRimuovi}"></i> Rimuovi
                    </button>
                </#if>
            </form>

        <#elseif mostraQuantita>
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

            <#-- in modalità carrello il rimuovi è già dentro il form quantità sopra -->
            <#if mostraRimuovi && !quantitaEditabile>
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
