begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|regionserver
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
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
name|hbase
operator|.
name|ServerName
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
name|hbase
operator|.
name|SharedConnection
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|hbase
operator|.
name|coprocessor
operator|.
name|BaseEnvironment
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
name|hbase
operator|.
name|coprocessor
operator|.
name|CoprocessorHost
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
name|hbase
operator|.
name|coprocessor
operator|.
name|CoprocessorServiceBackwardCompatiblity
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
name|hbase
operator|.
name|coprocessor
operator|.
name|CoreCoprocessor
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
name|hbase
operator|.
name|coprocessor
operator|.
name|HasRegionServerServices
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
name|hbase
operator|.
name|coprocessor
operator|.
name|MetricsCoprocessor
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
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionServerCoprocessor
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
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionServerCoprocessorEnvironment
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
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionServerObserver
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
name|hbase
operator|.
name|coprocessor
operator|.
name|SingletonCoprocessorService
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
name|hbase
operator|.
name|metrics
operator|.
name|MetricRegistry
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
name|hbase
operator|.
name|replication
operator|.
name|ReplicationEndpoint
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
name|hbase
operator|.
name|security
operator|.
name|User
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerCoprocessorHost
extends|extends
name|CoprocessorHost
argument_list|<
name|RegionServerCoprocessor
argument_list|,
name|RegionServerCoprocessorEnvironment
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RegionServerCoprocessorHost
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|public
name|RegionServerCoprocessorHost
parameter_list|(
name|RegionServerServices
name|rsServices
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|rsServices
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// Log the state of coprocessor loading here; should appear only once or
comment|// twice in the daemon log, depending on HBase version, because there is
comment|// only one RegionServerCoprocessorHost instance in the RS process
name|boolean
name|coprocessorsEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
name|DEFAULT_COPROCESSORS_ENABLED
argument_list|)
decl_stmt|;
name|boolean
name|tableCoprocessorsEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|USER_COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
name|DEFAULT_USER_COPROCESSORS_ENABLED
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"System coprocessor loading is "
operator|+
operator|(
name|coprocessorsEnabled
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table coprocessor loading is "
operator|+
operator|(
operator|(
name|coprocessorsEnabled
operator|&&
name|tableCoprocessorsEnabled
operator|)
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
name|loadSystemCoprocessors
argument_list|(
name|conf
argument_list|,
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerEnvironment
name|createEnvironment
parameter_list|(
name|RegionServerCoprocessor
name|instance
parameter_list|,
name|int
name|priority
parameter_list|,
name|int
name|sequence
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
comment|// If a CoreCoprocessor, return a 'richer' environment, one laden with RegionServerServices.
return|return
name|instance
operator|.
name|getClass
argument_list|()
operator|.
name|isAnnotationPresent
argument_list|(
name|CoreCoprocessor
operator|.
name|class
argument_list|)
condition|?
operator|new
name|RegionServerEnvironmentForCoreCoprocessors
argument_list|(
name|instance
argument_list|,
name|priority
argument_list|,
name|sequence
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|rsServices
argument_list|)
else|:
operator|new
name|RegionServerEnvironment
argument_list|(
name|instance
argument_list|,
name|priority
argument_list|,
name|sequence
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|rsServices
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerCoprocessor
name|checkAndGetInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|)
throws|throws
name|InstantiationException
throws|,
name|IllegalAccessException
block|{
try|try
block|{
if|if
condition|(
name|RegionServerCoprocessor
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|implClass
argument_list|)
condition|)
block|{
return|return
name|implClass
operator|.
name|asSubclass
argument_list|(
name|RegionServerCoprocessor
operator|.
name|class
argument_list|)
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|SingletonCoprocessorService
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|implClass
argument_list|)
condition|)
block|{
comment|// For backward compatibility with old CoprocessorService impl which don't extend
comment|// RegionCoprocessor.
name|SingletonCoprocessorService
name|tmp
init|=
name|implClass
operator|.
name|asSubclass
argument_list|(
name|SingletonCoprocessorService
operator|.
name|class
argument_list|)
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
return|return
operator|new
name|CoprocessorServiceBackwardCompatiblity
operator|.
name|RegionServerCoprocessorService
argument_list|(
name|tmp
argument_list|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"{} is not of type RegionServerCoprocessor. Check the configuration of {}"
argument_list|,
name|implClass
operator|.
name|getName
argument_list|()
argument_list|,
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InstantiationException
operator|)
operator|new
name|InstantiationException
argument_list|(
name|implClass
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ObserverGetter
argument_list|<
name|RegionServerCoprocessor
argument_list|,
name|RegionServerObserver
argument_list|>
name|rsObserverGetter
init|=
name|RegionServerCoprocessor
operator|::
name|getRegionServerObserver
decl_stmt|;
specifier|abstract
class|class
name|RegionServerObserverOperation
extends|extends
name|ObserverOperationWithoutResult
argument_list|<
name|RegionServerObserver
argument_list|>
block|{
specifier|public
name|RegionServerObserverOperation
parameter_list|()
block|{
name|super
argument_list|(
name|rsObserverGetter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionServerObserverOperation
parameter_list|(
name|User
name|user
parameter_list|)
block|{
name|super
argument_list|(
name|rsObserverGetter
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
block|}
comment|//////////////////////////////////////////////////////////////////////////////////////////////////
comment|// RegionServerObserver operations
comment|//////////////////////////////////////////////////////////////////////////////////////////////////
specifier|public
name|void
name|preStop
parameter_list|(
name|String
name|message
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
comment|// While stopping the region server all coprocessors method should be executed first then the
comment|// coprocessor should be cleaned up.
if|if
condition|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|execShutdown
argument_list|(
operator|new
name|RegionServerObserverOperation
argument_list|(
name|user
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|preStopRegionServer
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postEnvCall
parameter_list|()
block|{
comment|// invoke coprocessor stop method
name|shutdown
argument_list|(
name|this
operator|.
name|getEnvironment
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|preRollWALWriterRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|preRollWALWriterRequest
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postRollWALWriterRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|postRollWALWriterRequest
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|preReplicateLogEntries
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|preReplicateLogEntries
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postReplicateLogEntries
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|postReplicateLogEntries
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationEndpoint
name|postCreateReplicationEndPoint
parameter_list|(
specifier|final
name|ReplicationEndpoint
name|endpoint
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|endpoint
return|;
block|}
return|return
name|execOperationWithResult
argument_list|(
operator|new
name|ObserverOperationWithResult
argument_list|<
name|RegionServerObserver
argument_list|,
name|ReplicationEndpoint
argument_list|>
argument_list|(
name|rsObserverGetter
argument_list|,
name|endpoint
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|ReplicationEndpoint
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|observer
operator|.
name|postCreateReplicationEndPoint
argument_list|(
name|this
argument_list|,
name|getResult
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|void
name|preClearCompactionQueues
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|preClearCompactionQueues
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postClearCompactionQueues
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|postClearCompactionQueues
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|preExecuteProcedures
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|preExecuteProcedures
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postExecuteProcedures
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocEnvironments
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|RegionServerObserverOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|observer
parameter_list|)
throws|throws
name|IOException
block|{
name|observer
operator|.
name|postExecuteProcedures
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Coprocessor environment extension providing access to region server    * related services.    */
specifier|private
specifier|static
class|class
name|RegionServerEnvironment
extends|extends
name|BaseEnvironment
argument_list|<
name|RegionServerCoprocessor
argument_list|>
implements|implements
name|RegionServerCoprocessorEnvironment
block|{
specifier|private
specifier|final
name|MetricRegistry
name|metricRegistry
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|services
decl_stmt|;
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"BC_UNCONFIRMED_CAST"
argument_list|,
name|justification
operator|=
literal|"Intentional; FB has trouble detecting isAssignableFrom"
argument_list|)
specifier|public
name|RegionServerEnvironment
parameter_list|(
specifier|final
name|RegionServerCoprocessor
name|impl
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RegionServerServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
name|impl
argument_list|,
name|priority
argument_list|,
name|seq
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// If coprocessor exposes any services, register them.
for|for
control|(
name|Service
name|service
range|:
name|impl
operator|.
name|getServices
argument_list|()
control|)
block|{
name|services
operator|.
name|registerService
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|metricRegistry
operator|=
name|MetricsCoprocessor
operator|.
name|createRegistryForRSCoprocessor
argument_list|(
name|impl
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|OnlineRegions
name|getOnlineRegions
parameter_list|()
block|{
return|return
name|this
operator|.
name|services
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|services
operator|.
name|getServerName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|getConnection
parameter_list|()
block|{
return|return
operator|new
name|SharedConnection
argument_list|(
name|this
operator|.
name|services
operator|.
name|getConnection
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|services
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetricRegistry
name|getMetricRegistryForRegionServer
parameter_list|()
block|{
return|return
name|metricRegistry
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|super
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|MetricsCoprocessor
operator|.
name|removeRegistry
argument_list|(
name|metricRegistry
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Special version of RegionServerEnvironment that exposes RegionServerServices for Core    * Coprocessors only. Temporary hack until Core Coprocessors are integrated into Core.    */
specifier|private
specifier|static
class|class
name|RegionServerEnvironmentForCoreCoprocessors
extends|extends
name|RegionServerEnvironment
implements|implements
name|HasRegionServerServices
block|{
specifier|final
name|RegionServerServices
name|regionServerServices
decl_stmt|;
specifier|public
name|RegionServerEnvironmentForCoreCoprocessors
parameter_list|(
specifier|final
name|RegionServerCoprocessor
name|impl
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RegionServerServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
name|impl
argument_list|,
name|priority
argument_list|,
name|seq
argument_list|,
name|conf
argument_list|,
name|services
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServerServices
operator|=
name|services
expr_stmt|;
block|}
comment|/**      * @return An instance of RegionServerServices, an object NOT for general user-space Coprocessor      * consumption.      */
annotation|@
name|Override
specifier|public
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionServerServices
return|;
block|}
block|}
block|}
end_class

end_unit

