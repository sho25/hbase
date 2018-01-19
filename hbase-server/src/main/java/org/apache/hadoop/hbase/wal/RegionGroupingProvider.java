begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wal
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|AbstractFSWALProvider
operator|.
name|META_WAL_PROVIDER_ID
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|AbstractFSWALProvider
operator|.
name|WAL_FILE_NAME_DELIMITER
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Lock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|HConstants
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
name|RegionInfo
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
name|regionserver
operator|.
name|wal
operator|.
name|MetricsWAL
import|;
end_import

begin_comment
comment|// imports for classes still in regionserver.wal
end_comment

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
name|regionserver
operator|.
name|wal
operator|.
name|WALActionsListener
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|KeyLocker
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

begin_comment
comment|/**  * A WAL Provider that returns a WAL per group of regions.  *  * This provider follows the decorator pattern and mainly holds the logic for WAL grouping.  * WAL creation/roll/close is delegated to {@link #DELEGATE_PROVIDER}  *  * Region grouping is handled via {@link RegionGroupingStrategy} and can be configured via the  * property "hbase.wal.regiongrouping.strategy". Current strategy choices are  *<ul>  *<li><em>defaultStrategy</em> : Whatever strategy this version of HBase picks. currently  *                                  "bounded".</li>  *<li><em>identity</em> : each region belongs to its own group.</li>  *<li><em>bounded</em> : bounded number of groups and region evenly assigned to each group.</li>  *</ul>  * Optionally, a FQCN to a custom implementation may be given.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionGroupingProvider
implements|implements
name|WALProvider
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
name|RegionGroupingProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Map identifiers to a group number.    */
specifier|public
specifier|static
interface|interface
name|RegionGroupingStrategy
block|{
name|String
name|GROUP_NAME_DELIMITER
init|=
literal|"."
decl_stmt|;
comment|/**      * Given an identifier and a namespace, pick a group.      */
name|String
name|group
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|,
name|byte
index|[]
name|namespace
parameter_list|)
function_decl|;
name|void
name|init
parameter_list|(
name|Configuration
name|config
parameter_list|,
name|String
name|providerId
parameter_list|)
function_decl|;
block|}
comment|/**    * Maps between configuration names for strategies and implementation classes.    */
specifier|static
enum|enum
name|Strategies
block|{
name|defaultStrategy
parameter_list|(
name|BoundedGroupingStrategy
operator|.
name|class
parameter_list|)
operator|,
constructor|identity(IdentityGroupingStrategy.class
block|)
enum|,
name|bounded
parameter_list|(
name|BoundedGroupingStrategy
operator|.
name|class
parameter_list|)
operator|,
constructor|namespace(NamespaceGroupingStrategy.class
block|)
class|;
end_class

begin_decl_stmt
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|RegionGroupingStrategy
argument_list|>
name|clazz
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|Strategies
argument_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RegionGroupingStrategy
argument_list|>
name|clazz
argument_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
block|;     }
end_expr_stmt

begin_comment
unit|}
comment|/**    * instantiate a strategy from a config property.    * requires conf to have already been set (as well as anything the provider might need to read).    */
end_comment

begin_expr_stmt
unit|RegionGroupingStrategy
name|getStrategy
argument_list|(
name|final
name|Configuration
name|conf
argument_list|,
name|final
name|String
name|key
argument_list|,
name|final
name|String
name|defaultValue
argument_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|RegionGroupingStrategy
argument_list|>
name|clazz
expr_stmt|;
end_expr_stmt

begin_try
try|try
block|{
name|clazz
operator|=
name|Strategies
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|,
name|defaultValue
argument_list|)
argument_list|)
operator|.
name|clazz
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
comment|// Fall back to them specifying a class name
comment|// Note that the passed default class shouldn't actually be used, since the above only fails
comment|// when there is a config value present.
name|clazz
operator|=
name|conf
operator|.
name|getClass
argument_list|(
name|key
argument_list|,
name|IdentityGroupingStrategy
operator|.
name|class
argument_list|,
name|RegionGroupingStrategy
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
end_try

begin_expr_stmt
name|LOG
operator|.
name|info
argument_list|(
literal|"Instantiating RegionGroupingStrategy of type "
operator|+
name|clazz
argument_list|)
expr_stmt|;
end_expr_stmt

begin_try
try|try
block|{
specifier|final
name|RegionGroupingStrategy
name|result
init|=
name|clazz
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|result
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|providerId
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"couldn't set up region grouping strategy, check config key "
operator|+
name|REGION_GROUPING_STRATEGY
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception details for failure to load region grouping strategy."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't set up region grouping strategy"
argument_list|,
name|e
argument_list|)
throw|;
block|}
end_try

begin_decl_stmt
unit|}    public
specifier|static
specifier|final
name|String
name|REGION_GROUPING_STRATEGY
init|=
literal|"hbase.wal.regiongrouping.strategy"
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_REGION_GROUPING_STRATEGY
init|=
name|Strategies
operator|.
name|defaultStrategy
operator|.
name|name
argument_list|()
decl_stmt|;
end_decl_stmt

begin_comment
comment|/** delegate provider for WAL creation/roll/close */
end_comment

begin_decl_stmt
specifier|public
specifier|static
specifier|final
name|String
name|DELEGATE_PROVIDER
init|=
literal|"hbase.wal.regiongrouping.delegate.provider"
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_DELEGATE_PROVIDER
init|=
name|WALFactory
operator|.
name|Providers
operator|.
name|defaultProvider
operator|.
name|name
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
specifier|final
name|String
name|META_WAL_GROUP_NAME
init|=
literal|"meta"
decl_stmt|;
end_decl_stmt

begin_comment
comment|/** A group-provider mapping, make sure one-one rather than many-one mapping */
end_comment

begin_decl_stmt
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|WALProvider
argument_list|>
name|cached
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|final
name|KeyLocker
argument_list|<
name|String
argument_list|>
name|createLock
init|=
operator|new
name|KeyLocker
argument_list|<>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|RegionGroupingStrategy
name|strategy
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|WALFactory
name|factory
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|Configuration
name|conf
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|String
name|providerId
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|providerClass
decl_stmt|;
end_decl_stmt

begin_function
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|WALFactory
name|factory
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|!=
name|strategy
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"WALProvider.init should only be called once."
argument_list|)
throw|;
block|}
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|factory
operator|=
name|factory
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|factory
operator|.
name|factoryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|providerId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|providerId
operator|.
name|startsWith
argument_list|(
name|WAL_FILE_NAME_DELIMITER
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|providerId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|WAL_FILE_NAME_DELIMITER
argument_list|)
operator|.
name|append
argument_list|(
name|providerId
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|providerId
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|strategy
operator|=
name|getStrategy
argument_list|(
name|conf
argument_list|,
name|REGION_GROUPING_STRATEGY
argument_list|,
name|DEFAULT_REGION_GROUPING_STRATEGY
argument_list|)
expr_stmt|;
name|this
operator|.
name|providerClass
operator|=
name|factory
operator|.
name|getProviderClass
argument_list|(
name|DELEGATE_PROVIDER
argument_list|,
name|DEFAULT_DELEGATE_PROVIDER
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|private
name|WALProvider
name|createProvider
parameter_list|(
name|String
name|group
parameter_list|)
throws|throws
name|IOException
block|{
name|WALProvider
name|provider
init|=
name|WALFactory
operator|.
name|createProvider
argument_list|(
name|providerClass
argument_list|)
decl_stmt|;
name|provider
operator|.
name|init
argument_list|(
name|factory
argument_list|,
name|conf
argument_list|,
name|META_WAL_PROVIDER_ID
operator|.
name|equals
argument_list|(
name|providerId
argument_list|)
condition|?
name|META_WAL_PROVIDER_ID
else|:
name|group
argument_list|)
expr_stmt|;
name|provider
operator|.
name|addWALActionsListener
argument_list|(
operator|new
name|MetricsWAL
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|provider
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
block|{
return|return
name|cached
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|p
lambda|->
name|p
operator|.
name|getWALs
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_function
specifier|private
name|WAL
name|getWAL
parameter_list|(
name|String
name|group
parameter_list|)
throws|throws
name|IOException
block|{
name|WALProvider
name|provider
init|=
name|cached
operator|.
name|get
argument_list|(
name|group
argument_list|)
decl_stmt|;
if|if
condition|(
name|provider
operator|==
literal|null
condition|)
block|{
name|Lock
name|lock
init|=
name|createLock
operator|.
name|acquireLock
argument_list|(
name|group
argument_list|)
decl_stmt|;
try|try
block|{
name|provider
operator|=
name|cached
operator|.
name|get
argument_list|(
name|group
argument_list|)
expr_stmt|;
if|if
condition|(
name|provider
operator|==
literal|null
condition|)
block|{
name|provider
operator|=
name|createProvider
argument_list|(
name|group
argument_list|)
expr_stmt|;
name|listeners
operator|.
name|forEach
argument_list|(
name|provider
operator|::
name|addWALActionsListener
argument_list|)
expr_stmt|;
name|cached
operator|.
name|put
argument_list|(
name|group
argument_list|,
name|provider
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|provider
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|group
decl_stmt|;
if|if
condition|(
name|META_WAL_PROVIDER_ID
operator|.
name|equals
argument_list|(
name|this
operator|.
name|providerId
argument_list|)
condition|)
block|{
name|group
operator|=
name|META_WAL_GROUP_NAME
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|id
decl_stmt|;
name|byte
index|[]
name|namespace
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|id
operator|=
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
expr_stmt|;
name|namespace
operator|=
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespace
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|id
operator|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
expr_stmt|;
name|namespace
operator|=
literal|null
expr_stmt|;
block|}
name|group
operator|=
name|strategy
operator|.
name|group
argument_list|(
name|id
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
block|}
return|return
name|getWAL
argument_list|(
name|group
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
comment|// save the last exception and rethrow
name|IOException
name|failure
init|=
literal|null
decl_stmt|;
for|for
control|(
name|WALProvider
name|provider
range|:
name|cached
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|provider
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem shutting down wal provider '"
operator|+
name|provider
operator|+
literal|"': "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Details of problem shutting down wal provider '"
operator|+
name|provider
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|failure
operator|=
name|e
expr_stmt|;
block|}
block|}
if|if
condition|(
name|failure
operator|!=
literal|null
condition|)
block|{
throw|throw
name|failure
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// save the last exception and rethrow
name|IOException
name|failure
init|=
literal|null
decl_stmt|;
for|for
control|(
name|WALProvider
name|provider
range|:
name|cached
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|provider
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem closing wal provider '"
operator|+
name|provider
operator|+
literal|"': "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Details of problem closing wal provider '"
operator|+
name|provider
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|failure
operator|=
name|e
expr_stmt|;
block|}
block|}
if|if
condition|(
name|failure
operator|!=
literal|null
condition|)
block|{
throw|throw
name|failure
throw|;
block|}
block|}
end_function

begin_class
specifier|static
class|class
name|IdentityGroupingStrategy
implements|implements
name|RegionGroupingStrategy
block|{
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|config
parameter_list|,
name|String
name|providerId
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|String
name|group
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|,
specifier|final
name|byte
index|[]
name|namespace
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|identifier
argument_list|)
return|;
block|}
block|}
end_class

begin_function
annotation|@
name|Override
specifier|public
name|long
name|getNumLogFiles
parameter_list|()
block|{
name|long
name|numLogFiles
init|=
literal|0
decl_stmt|;
for|for
control|(
name|WALProvider
name|provider
range|:
name|cached
operator|.
name|values
argument_list|()
control|)
block|{
name|numLogFiles
operator|+=
name|provider
operator|.
name|getNumLogFiles
argument_list|()
expr_stmt|;
block|}
return|return
name|numLogFiles
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|long
name|getLogFileSize
parameter_list|()
block|{
name|long
name|logFileSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|WALProvider
name|provider
range|:
name|cached
operator|.
name|values
argument_list|()
control|)
block|{
name|logFileSize
operator|+=
name|provider
operator|.
name|getLogFileSize
argument_list|()
expr_stmt|;
block|}
return|return
name|logFileSize
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|addWALActionsListener
parameter_list|(
name|WALActionsListener
name|listener
parameter_list|)
block|{
comment|// Notice that there is an assumption that this method must be called before the getWAL above,
comment|// so we can make sure there is no sub WALProvider yet, so we only add the listener to our
comment|// listeners list without calling addWALActionListener for each WALProvider. Although it is no
comment|// hurt to execute an extra loop to call addWALActionListener for each WALProvider, but if the
comment|// extra code actually works, then we will have other big problems. So leave it as is.
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

