import { createStore } from 'vuex'

// store modules
import authModule from './auth'
import configuration from './configuration'
import deviceModule from './device'
import fileEditorModule from './fileEditor'
import graphModule from './graph'
import ifServicesModule from './ifServices'
import ipInterfacesModule from './ipInterfaces'
import logsModule from './logs'
import mapModule from './map'
import pluginModule from './plugin'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'
import usageStatisticsModule from './usageStatistics'

export default createStore({
  modules: {
    authModule,
    configuration,
    deviceModule,
    fileEditorModule,
    graphModule,
    ifServicesModule,
    ipInterfacesModule,
    logsModule,
    mapModule,
    pluginModule,
    resourceModule,
    scvModule,
    searchModule,
    usageStatisticsModule
  }
})
