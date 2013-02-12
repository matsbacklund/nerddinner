window.NerdDinner =
  MapDivId: 'theMap'
  _map: null
  _points: []
  _shapes: []
  ipInfoDbKey: ''
  BingMapsKey: ''

  LoadMap: (latitude, longitude, onMapLoaded) ->
    this._map = new VEMap(this.MapDivId)
    options = new VEMapOptions()
    options.EnableBirdseye = false

    ###Makes the control bar less obtrusize.###
    this._map.SetDashboardSize(VEDashboardSize.Small);

    this._map.onLoadMap = onMapLoaded if onMapLoaded?
    center = new VELatLong(latitude, longitude) if latitude? and longitude
    this._map.LoadMap center, null, null, null, null, null, null, options

  ClearMap: ->
    _map?.Clear()
    this._points = []
    this._shapes = []

  LoadPin: (LL, name, description, draggable) ->
    return if LL.Latitude is 0 or LL.Longitude is 0

    shape = new VEShape(VEShapeType.Pushpin, LL)

    if draggable
      shape.Draggable = true
      shape.onenddrag = this.onEndDrag

    ###Make a nice Pushpin shape with a title and description###
    shape.SetTitle "<span class=\"pinTitle\"> " + escape(name) + "</span>"

    shape.SetDescription("<p class=\"pinDetails\">" + escape(description) + "</p>") if description?

    this._map.AddShape shape
    this._points.push LL
    this._shapes.push shape

  FindAddressOnMap: (where) ->
    numberOfResults = 1
    setBestMapView = true
    showResults = true
    defaultDisambiguation = true

    this._map.Find "", where, null, null, null,
    numberOfResults, showResults, true, defaultDisambiguation,
    setBestMapView, this._callbackForLocation

  _callbackForLocation: (layer, resultsArray, places, hasMore, VEErrorMessage) ->
    this.ClearMap

    if !places?
      this._map.ShowMessage(VEErrorMessage)
      return

    ###Make a pushpin for each place we find###
    $.each places, (i, item) ->
      description = ""
      description = item.Description if item?
      LL = new VELatLong item.LatLong.Latitude, item.LatLong.Longitude

      window.NerdDinner.LoadPin LL, item.Name, description, true

    ###Make sure all pushpins are visible###
    window.NerdDinner._map.SetMapView(window.NerdDinner._points) if window.NerdDinner._points.length > 1

    ###If we've found exactly one place, that's our address.
    lat/long precision was getting lost here with toLocaleString, changed to toString###
    if window.NerdDinner._points.length is 1
      $("#latitude").val window.NerdDinner._points[0].Latitude.toString()
      $("#longitude").val window.NerdDinner._points[0].Longitude.toString()

  FindDinnersGivenLocation: (where) ->
    this._map.Find "", where, null, null, null, null, null, false, null, null, this._callbackUpdateMapDinners

  FindMostPopularDinners: (limit) ->
    $.post "/Search/GetMostPopularDinners", { "limit": limit }, this._renderDinners, "json"

  _callbackUpdateMapDinners: (layer, resultsArray, places, hasMore, VEErrorMessage) ->
    center = window.NerdDinner._map.GetCenter()

    $.post "/Search/SearchByLocation", { latitude: center.Latitude, longitude: center.Longitude }, window.NerdDinner._renderDinners, "json"

  _renderDinners: (dinners) ->
    $("#dinnerList").empty()
    this.ClearMap

    $.each dinners, (i, dinner) ->
      LL = new VELatLong dinner.Latitude, dinner.Longitude, 0, null

      ###Add Pin to Map###
      window.NerdDinner.LoadPin LL, _getDinnerLinkHTML(dinner), _getDinnerDescriptionHTML(dinner), false

      ###Add a dinner to the <ul> dinnerList on the right###
      $("#dinnerList").append($("<li/>")
        .attr("class", "dinnerItem")
        .append(_getDinnerLinkHTML(dinner))
        .append($("<br/>"))
        .append(_getDinnerDate(dinner, "mmm d"))
        .append(" with " + _getRSVPMessage(dinner.RSVPCount)))

    ###Adjust zoom to display all the pins we just added.###
    window.NerdDinner._map.SetMapView(this._points) if this._points.length > 1

    ###Display the event's pin-bubble on hover.###
    $(".dinnerItem").each (i, dinner) ->
      $(dinner).hover(
        -> window.NerdDinner._map.ShowInfoBox window.NerdDinner._shapes[i] ,
        -> window.NerdDinner._map.HideInfoBox window.NerdDinner._shapes[i] )

    _getDinnerDate (dinner, formatStr) ->
      '<strong>' + _dateDeserialize(dinner.EventDate).format(formatStr) + '</strong>'

    _getDinnerLinkHTML (dinner) ->
      '<a href="' + dinner.Url + '">' + dinner.Title + '</a>'

    _getDinnerDescriptionHTML (dinner) ->
      '<p>' + _getDinnerDate(dinner, "mmmm d, yyyy") + '</p><p>' + dinner.Description + '</p>' + _getRSVPMessage(dinner.RSVPCount)

    _dateDeserialize (dateStr) ->
      eval 'new' + dateStr.replace(/\//g, ' ')

    _getRSVPMessage (RSVPCount) ->
      rsvpMessage = "" + RSVPCount + " RSVP"
      rsvpMessage += "s" if RSVPCount > 1

  onEndDrag: (e) ->
    $("#latitude").val e.LatLong.Latitude.toString()
    $("#longitude").val e.LatLong.Longitude.toString()

  getLocationResults: (locations) ->
    if locations?
      currentAddress = $("#address")
      if locations[0].Name != currentAddress
        answer = confirm("Bing Maps returned the address '" + locations[0].Name + "' for the pin location. Click 'OK' to use this address for the event, or 'Cancel' to use the current address of '" + currentAddress.val() + "'")
        currentAddress.val(locations[0].Name) if answer

  getCurrentLocationByIpAddress: ->
    $.getJSON "http://api.ipinfodb.com/v3/ip-city/?format=json&callback=?&key=" + this.ipInfoDbKey,
    (data) ->
      $('#location').val(data.regionName + ', ' + data.countryName) if data.RegionName != ''

  getCurrentLocationByLatLong: (latitude, longitude) ->
    requestUrl = 'http://dev.virtualearth.net/REST/v1/Locations/' + latitude + ',' + longitude + '?key=' + this.BingMapsKey + '&jsonp=?'
    $.getJSON requestUrl,
    (result) ->
      $("#location").val(result.resourceSets[0].resources[0].address.formattedAddress) if result.resourceSets[0].estimatedTotal > 0