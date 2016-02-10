angular.module 'demo', []

.controller 'DemoController', class
  constructor: (@$http) ->
    @name = 'world'
    @refresh()

  refresh: ->
    if @name == ''
      @greeting = ''
      return

    @$http.get("/hello/#{@name}").success (data) =>
      @greeting = data