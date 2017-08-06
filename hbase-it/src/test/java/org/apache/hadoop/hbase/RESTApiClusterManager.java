begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configured
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|glassfish
operator|.
name|jersey
operator|.
name|client
operator|.
name|authentication
operator|.
name|HttpAuthenticationFeature
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|client
operator|.
name|ClientBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|client
operator|.
name|Entity
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|client
operator|.
name|Invocation
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|client
operator|.
name|WebTarget
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|MediaType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|Response
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|UriBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|ws
operator|.
name|http
operator|.
name|HTTPException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * A ClusterManager implementation designed to control Cloudera Manager (http://www.cloudera.com)  * clusters via REST API. This API uses HTTP GET requests against the cluster manager server to  * retrieve information and POST/PUT requests to perform actions. As a simple example, to retrieve a  * list of hosts from a CM server with login credentials admin:admin, a simple curl command would be  *     curl -X POST -H "Content-Type:application/json" -u admin:admin \  *         "http://this.is.my.server.com:7180/api/v8/hosts"  *  * This command would return a JSON result, which would need to be parsed to retrieve relevant  * information. This action and many others are covered by this class.  *  * A note on nomenclature: while the ClusterManager interface uses a ServiceType enum when  * referring to things like RegionServers and DataNodes, cluster managers often use different  * terminology. As an example, Cloudera Manager (http://www.cloudera.com) would refer to a  * RegionServer as a "role" of the HBase "service." It would further refer to "hbase" as the  * "serviceType." Apache Ambari (http://ambari.apache.org) would call the RegionServer a  * "component" of the HBase "service."  *  * This class will defer to the ClusterManager terminology in methods that it implements from  * that interface, but uses Cloudera Manager's terminology when dealing with its API directly.  */
end_comment

begin_class
specifier|public
class|class
name|RESTApiClusterManager
extends|extends
name|Configured
implements|implements
name|ClusterManager
block|{
comment|// Properties that need to be in the Configuration object to interact with the REST API cluster
comment|// manager. Most easily defined in hbase-site.xml, but can also be passed on the command line.
specifier|private
specifier|static
specifier|final
name|String
name|REST_API_CLUSTER_MANAGER_HOSTNAME
init|=
literal|"hbase.it.clustermanager.restapi.hostname"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REST_API_CLUSTER_MANAGER_USERNAME
init|=
literal|"hbase.it.clustermanager.restapi.username"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REST_API_CLUSTER_MANAGER_PASSWORD
init|=
literal|"hbase.it.clustermanager.restapi.password"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REST_API_CLUSTER_MANAGER_CLUSTER_NAME
init|=
literal|"hbase.it.clustermanager.restapi.clustername"
decl_stmt|;
comment|// Some default values for the above properties.
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_SERVER_HOSTNAME
init|=
literal|"http://localhost:7180"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_SERVER_USERNAME
init|=
literal|"admin"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_SERVER_PASSWORD
init|=
literal|"admin"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_CLUSTER_NAME
init|=
literal|"Cluster 1"
decl_stmt|;
comment|// Fields for the hostname, username, password, and cluster name of the cluster management server
comment|// to be used.
specifier|private
name|String
name|serverHostname
decl_stmt|;
specifier|private
name|String
name|serverUsername
decl_stmt|;
specifier|private
name|String
name|serverPassword
decl_stmt|;
specifier|private
name|String
name|clusterName
decl_stmt|;
comment|// Each version of Cloudera Manager supports a particular API versions. Version 6 of this API
comment|// provides all the features needed by this class.
specifier|private
specifier|static
specifier|final
name|String
name|API_VERSION
init|=
literal|"v6"
decl_stmt|;
comment|// Client instances are expensive, so use the same one for all our REST queries.
specifier|private
name|Client
name|client
init|=
name|ClientBuilder
operator|.
name|newClient
argument_list|()
decl_stmt|;
comment|// An instance of HBaseClusterManager is used for methods like the kill, resume, and suspend
comment|// because cluster managers don't tend to implement these operations.
specifier|private
name|ClusterManager
name|hBaseClusterManager
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RESTApiClusterManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|RESTApiClusterManager
parameter_list|()
block|{
name|hBaseClusterManager
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|HBaseClusterManager
operator|.
name|class
argument_list|,
operator|new
name|IntegrationTestingUtility
argument_list|()
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
comment|// Configured gets passed null before real conf. Why? I don't know.
return|return;
block|}
name|serverHostname
operator|=
name|conf
operator|.
name|get
argument_list|(
name|REST_API_CLUSTER_MANAGER_HOSTNAME
argument_list|,
name|DEFAULT_SERVER_HOSTNAME
argument_list|)
expr_stmt|;
name|serverUsername
operator|=
name|conf
operator|.
name|get
argument_list|(
name|REST_API_CLUSTER_MANAGER_USERNAME
argument_list|,
name|DEFAULT_SERVER_USERNAME
argument_list|)
expr_stmt|;
name|serverPassword
operator|=
name|conf
operator|.
name|get
argument_list|(
name|REST_API_CLUSTER_MANAGER_PASSWORD
argument_list|,
name|DEFAULT_SERVER_PASSWORD
argument_list|)
expr_stmt|;
name|clusterName
operator|=
name|conf
operator|.
name|get
argument_list|(
name|REST_API_CLUSTER_MANAGER_CLUSTER_NAME
argument_list|,
name|DEFAULT_CLUSTER_NAME
argument_list|)
expr_stmt|;
comment|// Add filter to Client instance to enable server authentication.
name|client
operator|.
name|register
argument_list|(
name|HttpAuthenticationFeature
operator|.
name|basic
argument_list|(
name|serverUsername
argument_list|,
name|serverPassword
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|performClusterManagerCommand
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|RoleCommand
operator|.
name|START
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|performClusterManagerCommand
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|RoleCommand
operator|.
name|STOP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|restart
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|performClusterManagerCommand
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|RoleCommand
operator|.
name|RESTART
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRunning
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|serviceName
init|=
name|getServiceName
argument_list|(
name|roleServiceType
operator|.
name|get
argument_list|(
name|service
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|hostId
init|=
name|getHostId
argument_list|(
name|hostname
argument_list|)
decl_stmt|;
name|String
name|roleState
init|=
name|getRoleState
argument_list|(
name|serviceName
argument_list|,
name|service
operator|.
name|toString
argument_list|()
argument_list|,
name|hostId
argument_list|)
decl_stmt|;
name|String
name|healthSummary
init|=
name|getHealthSummary
argument_list|(
name|serviceName
argument_list|,
name|service
operator|.
name|toString
argument_list|()
argument_list|,
name|hostId
argument_list|)
decl_stmt|;
name|boolean
name|isRunning
init|=
literal|false
decl_stmt|;
comment|// Use Yoda condition to prevent NullPointerException. roleState will be null if the "service
comment|// type" does not exist on the specified hostname.
if|if
condition|(
literal|"STARTED"
operator|.
name|equals
argument_list|(
name|roleState
argument_list|)
operator|&&
literal|"GOOD"
operator|.
name|equals
argument_list|(
name|healthSummary
argument_list|)
condition|)
block|{
name|isRunning
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|isRunning
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|kill
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|hBaseClusterManager
operator|.
name|kill
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|suspend
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|hBaseClusterManager
operator|.
name|suspend
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resume
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|hBaseClusterManager
operator|.
name|resume
argument_list|(
name|service
argument_list|,
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
comment|// Convenience method to execute command against role on hostname. Only graceful commands are
comment|// supported since cluster management APIs don't tend to let you SIGKILL things.
specifier|private
name|void
name|performClusterManagerCommand
parameter_list|(
name|ServiceType
name|role
parameter_list|,
name|String
name|hostname
parameter_list|,
name|RoleCommand
name|command
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing "
operator|+
name|command
operator|+
literal|" command against "
operator|+
name|role
operator|+
literal|" on "
operator|+
name|hostname
operator|+
literal|"..."
argument_list|)
expr_stmt|;
name|String
name|serviceName
init|=
name|getServiceName
argument_list|(
name|roleServiceType
operator|.
name|get
argument_list|(
name|role
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|hostId
init|=
name|getHostId
argument_list|(
name|hostname
argument_list|)
decl_stmt|;
name|String
name|roleName
init|=
name|getRoleName
argument_list|(
name|serviceName
argument_list|,
name|role
operator|.
name|toString
argument_list|()
argument_list|,
name|hostId
argument_list|)
decl_stmt|;
name|doRoleCommand
argument_list|(
name|serviceName
argument_list|,
name|roleName
argument_list|,
name|command
argument_list|)
expr_stmt|;
block|}
comment|// Performing a command (e.g. starting or stopping a role) requires a POST instead of a GET.
specifier|private
name|void
name|doRoleCommand
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|roleName
parameter_list|,
name|RoleCommand
name|roleCommand
parameter_list|)
block|{
name|URI
name|uri
init|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|serverHostname
argument_list|)
operator|.
name|path
argument_list|(
literal|"api"
argument_list|)
operator|.
name|path
argument_list|(
name|API_VERSION
argument_list|)
operator|.
name|path
argument_list|(
literal|"clusters"
argument_list|)
operator|.
name|path
argument_list|(
name|clusterName
argument_list|)
operator|.
name|path
argument_list|(
literal|"services"
argument_list|)
operator|.
name|path
argument_list|(
name|serviceName
argument_list|)
operator|.
name|path
argument_list|(
literal|"roleCommands"
argument_list|)
operator|.
name|path
argument_list|(
name|roleCommand
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|body
init|=
literal|"{ \"items\": [ \""
operator|+
name|roleName
operator|+
literal|"\" ] }"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing POST against "
operator|+
name|uri
operator|+
literal|" with body "
operator|+
name|body
operator|+
literal|"..."
argument_list|)
expr_stmt|;
name|WebTarget
name|webTarget
init|=
name|client
operator|.
name|target
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|Invocation
operator|.
name|Builder
name|invocationBuilder
init|=
name|webTarget
operator|.
name|request
argument_list|(
name|MediaType
operator|.
name|APPLICATION_JSON
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|invocationBuilder
operator|.
name|post
argument_list|(
name|Entity
operator|.
name|json
argument_list|(
name|body
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|statusCode
init|=
name|response
operator|.
name|getStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|statusCode
operator|!=
name|Response
operator|.
name|Status
operator|.
name|OK
operator|.
name|getStatusCode
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HTTPException
argument_list|(
name|statusCode
argument_list|)
throw|;
block|}
block|}
comment|// Possible healthSummary values include "GOOD" and "BAD."
specifier|private
name|String
name|getHealthSummary
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|roleType
parameter_list|,
name|String
name|hostId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRolePropertyValue
argument_list|(
name|serviceName
argument_list|,
name|roleType
argument_list|,
name|hostId
argument_list|,
literal|"healthSummary"
argument_list|)
return|;
block|}
comment|// This API uses a hostId to execute host-specific commands; get one from a hostname.
specifier|private
name|String
name|getHostId
parameter_list|(
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|hostId
init|=
literal|null
decl_stmt|;
name|URI
name|uri
init|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|serverHostname
argument_list|)
operator|.
name|path
argument_list|(
literal|"api"
argument_list|)
operator|.
name|path
argument_list|(
name|API_VERSION
argument_list|)
operator|.
name|path
argument_list|(
literal|"hosts"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|JsonNode
name|hosts
init|=
name|getJsonNodeFromURIGet
argument_list|(
name|uri
argument_list|)
decl_stmt|;
if|if
condition|(
name|hosts
operator|!=
literal|null
condition|)
block|{
comment|// Iterate through the list of hosts, stopping once you've reached the requested hostname.
for|for
control|(
name|JsonNode
name|host
range|:
name|hosts
control|)
block|{
if|if
condition|(
name|host
operator|.
name|get
argument_list|(
literal|"hostname"
argument_list|)
operator|.
name|getTextValue
argument_list|()
operator|.
name|equals
argument_list|(
name|hostname
argument_list|)
condition|)
block|{
name|hostId
operator|=
name|host
operator|.
name|get
argument_list|(
literal|"hostId"
argument_list|)
operator|.
name|getTextValue
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
else|else
block|{
name|hostId
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|hostId
return|;
block|}
comment|// Execute GET against URI, returning a JsonNode object to be traversed.
specifier|private
name|JsonNode
name|getJsonNodeFromURIGet
parameter_list|(
name|URI
name|uri
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing GET against "
operator|+
name|uri
operator|+
literal|"..."
argument_list|)
expr_stmt|;
name|WebTarget
name|webTarget
init|=
name|client
operator|.
name|target
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|Invocation
operator|.
name|Builder
name|invocationBuilder
init|=
name|webTarget
operator|.
name|request
argument_list|(
name|MediaType
operator|.
name|APPLICATION_JSON
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|invocationBuilder
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|statusCode
init|=
name|response
operator|.
name|getStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|statusCode
operator|!=
name|Response
operator|.
name|Status
operator|.
name|OK
operator|.
name|getStatusCode
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HTTPException
argument_list|(
name|statusCode
argument_list|)
throw|;
block|}
comment|// This API folds information as the value to an "items" attribute.
return|return
operator|new
name|ObjectMapper
argument_list|()
operator|.
name|readTree
argument_list|(
name|response
operator|.
name|readEntity
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"items"
argument_list|)
return|;
block|}
comment|// This API assigns a unique role name to each host's instance of a role.
specifier|private
name|String
name|getRoleName
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|roleType
parameter_list|,
name|String
name|hostId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRolePropertyValue
argument_list|(
name|serviceName
argument_list|,
name|roleType
argument_list|,
name|hostId
argument_list|,
literal|"name"
argument_list|)
return|;
block|}
comment|// Get the value of a  property from a role on a particular host.
specifier|private
name|String
name|getRolePropertyValue
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|roleType
parameter_list|,
name|String
name|hostId
parameter_list|,
name|String
name|property
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|roleValue
init|=
literal|null
decl_stmt|;
name|URI
name|uri
init|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|serverHostname
argument_list|)
operator|.
name|path
argument_list|(
literal|"api"
argument_list|)
operator|.
name|path
argument_list|(
name|API_VERSION
argument_list|)
operator|.
name|path
argument_list|(
literal|"clusters"
argument_list|)
operator|.
name|path
argument_list|(
name|clusterName
argument_list|)
operator|.
name|path
argument_list|(
literal|"services"
argument_list|)
operator|.
name|path
argument_list|(
name|serviceName
argument_list|)
operator|.
name|path
argument_list|(
literal|"roles"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|JsonNode
name|roles
init|=
name|getJsonNodeFromURIGet
argument_list|(
name|uri
argument_list|)
decl_stmt|;
if|if
condition|(
name|roles
operator|!=
literal|null
condition|)
block|{
comment|// Iterate through the list of roles, stopping once the requested one is found.
for|for
control|(
name|JsonNode
name|role
range|:
name|roles
control|)
block|{
if|if
condition|(
name|role
operator|.
name|get
argument_list|(
literal|"hostRef"
argument_list|)
operator|.
name|get
argument_list|(
literal|"hostId"
argument_list|)
operator|.
name|getTextValue
argument_list|()
operator|.
name|equals
argument_list|(
name|hostId
argument_list|)
operator|&&
name|role
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|getTextValue
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|equals
argument_list|(
name|roleType
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
condition|)
block|{
name|roleValue
operator|=
name|role
operator|.
name|get
argument_list|(
name|property
argument_list|)
operator|.
name|getTextValue
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|roleValue
return|;
block|}
comment|// Possible roleState values include "STARTED" and "STOPPED."
specifier|private
name|String
name|getRoleState
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|roleType
parameter_list|,
name|String
name|hostId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRolePropertyValue
argument_list|(
name|serviceName
argument_list|,
name|roleType
argument_list|,
name|hostId
argument_list|,
literal|"roleState"
argument_list|)
return|;
block|}
comment|// Convert a service (e.g. "HBASE," "HDFS") into a service name (e.g. "HBASE-1," "HDFS-1").
specifier|private
name|String
name|getServiceName
parameter_list|(
name|Service
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|serviceName
init|=
literal|null
decl_stmt|;
name|URI
name|uri
init|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|serverHostname
argument_list|)
operator|.
name|path
argument_list|(
literal|"api"
argument_list|)
operator|.
name|path
argument_list|(
name|API_VERSION
argument_list|)
operator|.
name|path
argument_list|(
literal|"clusters"
argument_list|)
operator|.
name|path
argument_list|(
name|clusterName
argument_list|)
operator|.
name|path
argument_list|(
literal|"services"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|JsonNode
name|services
init|=
name|getJsonNodeFromURIGet
argument_list|(
name|uri
argument_list|)
decl_stmt|;
if|if
condition|(
name|services
operator|!=
literal|null
condition|)
block|{
comment|// Iterate through the list of services, stopping once the requested one is found.
for|for
control|(
name|JsonNode
name|serviceEntry
range|:
name|services
control|)
block|{
if|if
condition|(
name|serviceEntry
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|getTextValue
argument_list|()
operator|.
name|equals
argument_list|(
name|service
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|serviceName
operator|=
name|serviceEntry
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|.
name|getTextValue
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|serviceName
return|;
block|}
comment|/*    * Some enums to guard against bad calls.    */
comment|// The RoleCommand enum is used by the doRoleCommand method to guard against non-existent methods
comment|// being invoked on a given role.
comment|// TODO: Integrate zookeeper and hdfs related failure injections (Ref: HBASE-14261).
specifier|private
enum|enum
name|RoleCommand
block|{
name|START
block|,
name|STOP
block|,
name|RESTART
block|;
comment|// APIs tend to take commands in lowercase, so convert them to save the trouble later.
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
comment|// ClusterManager methods take a "ServiceType" object (e.g. "HBASE_MASTER," "HADOOP_NAMENODE").
comment|// These "service types," which cluster managers call "roles" or "components," need to be mapped
comment|// to their corresponding service (e.g. "HBase," "HDFS") in order to be controlled.
specifier|private
specifier|static
name|Map
argument_list|<
name|ServiceType
argument_list|,
name|Service
argument_list|>
name|roleServiceType
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HADOOP_NAMENODE
argument_list|,
name|Service
operator|.
name|HDFS
argument_list|)
expr_stmt|;
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HADOOP_DATANODE
argument_list|,
name|Service
operator|.
name|HDFS
argument_list|)
expr_stmt|;
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HADOOP_JOBTRACKER
argument_list|,
name|Service
operator|.
name|MAPREDUCE
argument_list|)
expr_stmt|;
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HADOOP_TASKTRACKER
argument_list|,
name|Service
operator|.
name|MAPREDUCE
argument_list|)
expr_stmt|;
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|Service
operator|.
name|HBASE
argument_list|)
expr_stmt|;
name|roleServiceType
operator|.
name|put
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
name|Service
operator|.
name|HBASE
argument_list|)
expr_stmt|;
block|}
specifier|private
enum|enum
name|Service
block|{
name|HBASE
block|,
name|HDFS
block|,
name|MAPREDUCE
block|}
block|}
end_class

end_unit

