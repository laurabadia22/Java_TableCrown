<#macro barraRicerca actionUrl query="">
    <form method="get" action="${actionUrl}" class="home-search-form">
        <input class="input home-search-input" type="search" name="q" value="${query?html}" placeholder="Cerca...">
        <button class="button home-search-btn" type="submit"><i class="ti ti-search"></i></button>
    </form>
</#macro>