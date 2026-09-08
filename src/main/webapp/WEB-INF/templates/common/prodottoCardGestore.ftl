<#macro cardGestore p>
    <div class="product-card">

        <#-- IMMAGINE E BADGE -->
        <div class="product-image-wrapper">
            <div class="product-badges-left">
                <#if p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'ESAURITO'>
                    <span class="product-badge product-badge-esaurito">Esaurito</span>
                <#elseif p.disponibilitaProdotto?? && p.disponibilitaProdotto.name() == 'NON_DISPONIBILE'>
                    <span class="product-badge product-badge-non-disponibile">Nascosto</span>
                </#if>
            </div>
<#--            &lt;#&ndash; 1. Impostiamo un placeholder generico di base &ndash;&gt;-->
<#--            <#assign imgPlaceholder = "placeholder.png">-->

<#--            &lt;#&ndash; 2. Cambiamo il placeholder in base al tipo di prodotto &ndash;&gt;-->
<#--            <#if p.tipo??>-->
<#--                <#if p.tipo == "gioco">-->
<#--                    <#assign imgPlaceholder = "gioco_da_tavolo_img.jpg">-->
<#--                <#elseif p.tipo == "bustine" || p.tipo == "Bustine">-->
<#--                    <#assign imgPlaceholder = "bustine_img.jpg">-->
<#--                <#elseif p.tipo == "portaDadi" || p.tipo == "Portadadi">-->
<#--                    <#assign imgPlaceholder = "porta_dadi_img.jpg.png">-->
<#--                </#if>-->
<#--            </#if>-->

<#--            &lt;#&ndash; 3. Usiamo la singola riga intelligente con il placeholder dinamico &ndash;&gt;-->
<#--            <img src="${base_url}/${p.imgProdotto!''}"-->
<#--                 onerror="this.onerror=null; this.src='${base_url}/public/img/${imgPlaceholder}';"-->
<#--                 alt="${p.nomeProdotto?html}"-->
<#--                 class="product-image">-->

            <#-- 1. Leggiamo cosa c'è nel database (se è null, diventa vuoto "") -->
            <#assign imgDalDb = p.imgProdotto!"">

            <#-- 2. Logica del vigile urbano -->
            <#if imgDalDb == "">

            <#-- CASO A: Il prodotto non ha immagine. Scegliamo il placeholder in base al tipo -->
                <#assign nomePlaceholder = "placeholder.png">
                <#if p.tipo??>
                    <#if p.tipo == "gioco">-->
                        <#assign imgPlaceholder = "gioco_da_tavolo_img.jpg">
                    <#elseif p.tipo == "bustine" || p.tipo == "Bustine">
                        <#assign imgPlaceholder = "bustine_img.jpg">
                    <#elseif p.tipo == "portaDadi" || p.tipo == "Portadadi">
                        <#assign imgPlaceholder = "porta_dadi_img.jpg.png">
                    </#if>
                </#if>
                <#assign pathFinale = "${base_url}/public/img/prodotti/${nomePlaceholder}">

            <#elseif imgDalDb?contains("/")>

            <#-- CASO B: Prodotto NUOVO Java (contiene lo slash, es: uploads/prodotti/foto.jpg) -->
                <#assign pathFinale = "${base_url}/${imgDalDb}">

            <#else>

            <#-- CASO C: Prodotto VECCHIO PHP (es: dixit.jpg) o placeholder testuale -->
                <#assign pathFinale = "${base_url}/public/img/prodotti/${imgDalDb}">

            </#if>

            <#-- 3. Stampiamo il tag IMG pulitissimo -->
            <img src="${pathFinale}" alt="${p.nomeProdotto?html}" class="product-image">
        </div>

        <#-- INFORMAZIONI -->
        <div class="product-info">
            <h3 class="product-name">${p.nomeProdotto?html}</h3>
            <div class="product-price-wrapper">
                <#if (p.prezzoScontato < p.prezzo)>
                    <span class="product-price">${p.prezzoScontato?string("0.00")} €</span>
                    <span class="product-price-old">${p.prezzo?string("0.00")} €</span>
                <#else>
                    <span class="product-price">${p.prezzo?string("0.00")} €</span>
                </#if>
            </div>

            <hr class="my-3" style="background-color: #eee; height: 1px; border: none;">

            <#-- GESTIONE SCORTE (Punta a /gestore/prodotti/quantita) -->
            <div class="is-flex is-justify-content-space-between is-align-items-center mb-3">
                <span class="is-size-7 has-text-grey font-weight-bold">
                    <i class="ti ti-package"></i> In magazzino:
                </span>

                <form action="${base_url}/gestore/prodotti/quantita" method="POST" class="is-flex is-align-items-center">
                    <input type="hidden" name="id_prodotto" value="${p.idProdotto}">
                    <input type="hidden" name="delta_quantita" id="delta-${p.idProdotto}" value="0">

                    <button type="submit" class="button is-small is-light" onclick="document.getElementById('delta-${p.idProdotto}').value='-1'"><i class="ti ti-minus"></i></button>
                    <span class="px-3 font-weight-bold">${p.quantita}</span>
                    <button type="submit" class="button is-small is-light" onclick="document.getElementById('delta-${p.idProdotto}').value='1'"><i class="ti ti-plus"></i></button>
                </form>
            </div>

            <#-- AZIONI MODIFICA / ELIMINA -->
            <div class="columns is-mobile is-gapless m-0">
                <div class="column pr-1">
                    <button type="button" class="button is-info is-light is-fullwidth is-small btn-modifica" data-id="${p.idProdotto}">
                        <i class="ti ti-pencil mr-1"></i> Modifica
                    </button>
                </div>
                <div class="column pl-1">
                    <form action="${base_url}/gestore/catalogo/prodotto/elimina" method="POST" onsubmit="return confirm('Vuoi davvero nascondere questo prodotto dal catalogo?');">
                        <input type="hidden" name="id_prodotto" value="${p.idProdotto}">
                        <button type="submit" class="button is-danger is-light is-fullwidth is-small">
                            <i class="ti ti-trash mr-1"></i> Elimina
                        </button>
                    </form>
                </div>
            </div>

        </div>
    </div>
</#macro>