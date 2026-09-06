<#import "common/layout.ftl" as layout>

<#assign extra_css>
    <link rel="stylesheet" href="${base_url}/public/css/profiloIndirizzi.css">
</#assign>

<@layout.page page_title="I Miei Indirizzi - TableCrown" extra_css=extra_css extra_js="">

    <div class="indirizzi-container">
        <div class="container">

            <div class="indirizzi-topbar">
                <a href="${base_url}/profilo" class="indirizzi-back-link">
                    <i class="ti ti-arrow-left"></i> Torna all'Area Personale
                </a>
            </div>

            <div class="indirizzi-header">
                <div class="indirizzi-header-text">
                    <span class="indirizzi-eyebrow">Area Personale</span>
                    <h1 class="indirizzi-titolo">
                        <i class="ti ti-map-pin"></i> I Miei Indirizzi
                    </h1>
                </div>

                <#if indirizzi?has_content>
                    <div class="indirizzi-count-badge">
                        <span class="indirizzi-count-num">${indirizzi?size}</span>
                        <span class="indirizzi-count-label"><#if indirizzi?size == 1>indirizzo<#else>indirizzi</#if></span>
                    </div>
                </#if>
            </div>

            <#if indirizzi?has_content>
                <div class="indirizzi-list">
                    <#list indirizzi as indirizzo>
                        <div class="indirizzi-card <#if indirizzo.predefinito>indirizzi-card-predefinito</#if>">

                            <div class="indirizzi-card-top">
                                <span class="indirizzi-nome">${indirizzo.nome?html}</span>
                                <#if indirizzo.predefinito>
                                    <span class="indirizzi-predefinito-badge">
                                        <i class="ti ti-star-filled"></i> Predefinito
                                    </span>
                                </#if>
                            </div>

                            <div class="indirizzi-info-list">
                                <div class="indirizzi-info-row">
                                    <i class="ti ti-road"></i>
                                    <span class="indirizzi-info-value">${indirizzo.via?html}</span>
                                </div>
                                <div class="indirizzi-info-row">
                                    <i class="ti ti-building"></i>
                                    <span class="indirizzi-info-value">${indirizzo.citta?html} (${indirizzo.provincia?html}), ${indirizzo.cap?html}</span>
                                </div>
                                <div class="indirizzi-info-row">
                                    <i class="ti ti-flag"></i>
                                    <span class="indirizzi-info-value">${indirizzo.nazione?html}</span>
                                </div>
                                <#if indirizzo.nomeCitofono?has_content>
                                    <div class="indirizzi-info-row">
                                        <i class="ti ti-bell"></i>
                                        <span class="indirizzi-info-value">${indirizzo.nomeCitofono?html}</span>
                                    </div>
                                </#if>
                            </div>

                            <div class="indirizzi-actions">
                                <#if !indirizzo.predefinito>
                                    <form action="${base_url}/profilo/indirizzi/predefinito" method="POST" class="indirizzi-action-form">
                                        <input type="hidden" name="id_indirizzo" value="${indirizzo.idIndirizzo}">
                                        <button type="submit" class="indirizzi-btn-predefinito">
                                            <i class="ti ti-star"></i> Imposta come predefinito
                                        </button>
                                    </form>
                                </#if>

                                <form action="${base_url}/profilo/indirizzi/elimina" method="POST" class="indirizzi-action-form"
                                      onsubmit="return confirm('Sei sicuro di voler eliminare questo indirizzo?');">
                                    <input type="hidden" name="id_indirizzo" value="${indirizzo.idIndirizzo}">
                                    <button type="submit" class="indirizzi-btn-elimina">
                                        <i class="ti ti-trash"></i> Elimina
                                    </button>
                                </form>
                            </div>

                        </div>
                    </#list>
                </div>
            <#else>
                <div class="indirizzi-empty">
                    <div class="indirizzi-empty-icon">
                        <i class="ti ti-map-pin-off"></i>
                    </div>
                    <h2 class="indirizzi-empty-titolo">Nessun indirizzo salvato</h2>
                    <p class="indirizzi-empty-testo">Aggiungi il tuo primo indirizzo per velocizzare i tuoi prossimi ordini.</p>
                </div>
            </#if>

            <#-- ── FORM AGGIUNGI INDIRIZZO: <details> nativo, zero JS ── -->
            <details class="indirizzi-add-details">
                <summary class="indirizzi-add-summary">
                    <i class="ti ti-plus"></i> Aggiungi nuovo indirizzo
                </summary>

                <form action="${base_url}/profilo/indirizzi/aggiungi" method="POST" class="indirizzi-form">
                    <div class="indirizzi-form-grid">
                        <div class="indirizzi-form-field indirizzi-form-field-full">
                            <label class="indirizzi-form-label" for="indirizzi-form-nome">Nome indirizzo</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-nome" name="nome" placeholder="Es. Casa, Ufficio" required>
                        </div>

                        <div class="indirizzi-form-field indirizzi-form-field-full">
                            <label class="indirizzi-form-label" for="indirizzi-form-via">Via e numero civico</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-via" name="via" required>
                        </div>

                        <div class="indirizzi-form-field">
                            <label class="indirizzi-form-label" for="indirizzi-form-citta">Città</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-citta" name="citta" required>
                        </div>

                        <div class="indirizzi-form-field">
                            <label class="indirizzi-form-label" for="indirizzi-form-provincia">Provincia</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-provincia" name="provincia" maxlength="2" pattern="[A-Za-z]{2}" required>
                        </div>

                        <div class="indirizzi-form-field">
                            <label class="indirizzi-form-label" for="indirizzi-form-cap">CAP</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-cap" name="cap"
                                   maxlength="5" pattern="\d{5}" inputmode="numeric" title="Il CAP deve essere composto da 5 cifre"
                                   required>
                        </div>

                        <div class="indirizzi-form-field">
                            <label class="indirizzi-form-label" for="indirizzi-form-nazione">Nazione</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-nazione" name="nazione" required>
                        </div>

                        <div class="indirizzi-form-field indirizzi-form-field-full">
                            <label class="indirizzi-form-label" for="indirizzi-form-citofono">Nome sul citofono</label>
                            <input type="text" class="indirizzi-form-input" id="indirizzi-form-citofono" name="nomeCitofono" required>
                        </div>

                        <#if indirizzi?has_content>
                            <div class="indirizzi-form-field indirizzi-form-field-full">
                                <label class="indirizzi-form-checkbox-label">
                                    <input type="checkbox" name="predefinito" value="true">
                                    Imposta come indirizzo predefinito
                                </label>
                            </div>
                        </#if>
                    </div>

                    <div class="indirizzi-form-actions">
                        <button type="submit" class="indirizzi-form-btn-primary">Salva indirizzo</button>
                    </div>
                </form>
            </details>

        </div>
    </div>

</@layout.page>