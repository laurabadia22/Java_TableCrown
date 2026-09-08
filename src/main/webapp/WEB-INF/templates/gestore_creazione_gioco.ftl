<#import "common/formCreazioneProdotto.ftl" as fcp>

<@fcp.formCreazione
actionUrl="/gestore/catalogo/giochi-da-tavolo/nuovo"
urlAnnulla="/gestore/catalogo/giochi-da-tavolo"
titoloPagina="Nuovo Gioco"
currentSubpage="catalogo_giochi"
titoloForm="Aggiungi Gioco da Tavolo"
sottotitoloForm="Inserisci le regole e le specifiche del nuovo gioco da tavolo.">

    <hr>
    <h2 class="title is-5 mb-4 has-text-primary">Specifiche di Gioco</h2>
    <div class="columns is-multiline">

        <div class="column is-3">
            <div class="field">
                <label class="label">Lingua *</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="lingua" required>
                            <#list lingue_enum as l>
                                <option value="${l.name()}">${l.name()?replace("_", " ")}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Difficoltà *</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="difficolta" required>
                            <#list difficolta_enum as d>
                                <option value="${d.name()}">${d.name()?replace("_", " ")}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-6">
            <div class="field">
                <label class="label">Categoria (Selezione multipla) *</label>
                <div class="control">
                    <div class="select is-multiple is-fullwidth">
                        <select name="categoria" multiple size="3" required>
                            <#list categoria_enum as c>
                                <option value="${c.name()}">${c.name()?replace("_", " ")}</option>
                            </#list>
                        </select>
                    </div>
                    <p class="help">Tieni premuto CTRL (o CMD su Mac) per selezionare più categorie.</p>
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Giocatori Min *</label>
                <div class="control">
                    <input class="input" type="number" name="numeroGiocatoriMin" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Giocatori Max *</label>
                <div class="control">
                    <input class="input" type="number" name="numeroGiocatoriMax" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Età Minima *</label>
                <div class="control">
                    <input class="input" type="number" name="etaMinima" required min="1">
                </div>
            </div>
        </div>

        <div class="column is-3">
            <div class="field">
                <label class="label">Durata Media (min) *</label>
                <div class="control">
                    <input class="input" type="number" name="durataMedia" required min="1" step="5">
                </div>
            </div>
        </div>

        <div class="column is-6">
            <div class="field">
                <label class="label">È un'espansione? Seleziona il Gioco Base</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="giocoBaseId">
                            <option value="">Nessuno (È un gioco completo)</option>
                            <#list giochiDisponibili as giocoBase>
                                <option value="${giocoBase.idProdotto}">${giocoBase.nomeProdotto}</option>
                            </#list>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="column is-6">
            <div class="field">
                <label class="label">Componenti della scatola</label>
                <div class="control">
                    <input class="input" type="text" name="componenti" placeholder="es. 1 tabellone">
                    <input class="input mt-2" type="text" name="componenti" placeholder="es. 50 carte">
                    <input class="input mt-2" type="text" name="componenti" placeholder="es. 6 pedine">
                </div>
                <p class="help">Riempi quanti campi desideri (non obbligatorio).</p>
            </div>
        </div>

        <div class="column is-12 mt-4">
            <div class="box has-background-warning-light border-warning">
                <label class="checkbox mb-3 font-weight-bold has-text-warning-dark">
                    <input type="checkbox" name="danneggiato" value="true">
                    Il gioco presenta un danno?
                </label>
                <div class="columns">
                    <div class="column is-4">
                        <div class="field">
                            <label class="label is-small">Entità del Danno</label>
                            <div class="control">
                                <div class="select is-fullwidth is-small">
                                    <select name="livelloDanno">
                                        <#list danno_enum as danno>
                                            <option value="${danno.name()}">${danno.name()?replace("_", " ")}</option>
                                        </#list>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="column is-8">
                        <div class="field">
                            <label class="label is-small">Descrizione Danno</label>
                            <div class="control">
                                <input class="input is-small" type="text" name="descrizioneDanno" placeholder="es. Angolo della scatola ammaccato">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</@fcp.formCreazione>