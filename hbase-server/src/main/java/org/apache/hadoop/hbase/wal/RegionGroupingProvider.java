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
name|DefaultWALProvider
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
name|DefaultWALProvider
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
name|Collections
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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|fs
operator|.
name|FileSystem
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
name|regionserver
operator|.
name|wal
operator|.
name|FSHLog
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
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * A WAL Provider that returns a WAL per group of regions.  *  * Region grouping is handled via {@link RegionGroupingStrategy} and can be configured via the  * property "hbase.wal.regiongrouping.strategy". Current strategy choices are  *<ul>  *<li><em>defaultStrategy</em> : Whatever strategy this version of HBase picks. currently  *                                  "bounded".</li>  *<li><em>identity</em> : each region belongs to its own group.</li>  *<li><em>bounded</em> : bounded number of groups and region evenly assigned to each group.</li>  *</ul>  * Optionally, a FQCN to a custom implementation may be given.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RegionGroupingProvider
implements|implements
name|WALProvider
block|{
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
comment|/**      * Given an identifier, pick a group.      * the byte[] returned for a given group must always use the same instance, since we      * will be using it as a hash key.      */
name|String
name|group
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
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
constructor_decl|;
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|RegionGroupingStrategy
argument_list|>
name|clazz
decl_stmt|;
name|Strategies
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RegionGroupingStrategy
argument_list|>
name|clazz
parameter_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
block|}
block|}
end_class

begin_comment
comment|/**    * instantiate a strategy from a config property.    * requires conf to have already been set (as well as anything the provider might need to read).    */
end_comment

begin_function
name|RegionGroupingStrategy
name|getStrategy
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|String
name|defaultValue
parameter_list|)
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
decl_stmt|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Instantiating RegionGroupingStrategy of type "
operator|+
name|clazz
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|RegionGroupingStrategy
name|result
init|=
name|clazz
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
name|InstantiationException
name|exception
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
name|exception
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't set up region grouping strategy"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|exception
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
name|exception
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't set up region grouping strategy"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
end_function

begin_decl_stmt
specifier|public
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
comment|/** A group-wal mapping, recommended to make sure one-one rather than many-one mapping */
end_comment

begin_decl_stmt
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FSHLog
argument_list|>
name|cached
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|FSHLog
argument_list|>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_comment
comment|/** Stores unique wals generated by this RegionGroupingProvider */
end_comment

begin_decl_stmt
specifier|private
specifier|final
name|Set
argument_list|<
name|FSHLog
argument_list|>
name|logs
init|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|FSHLog
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
end_decl_stmt

begin_comment
comment|/**    * we synchronize on walCacheLock to prevent wal recreation in different threads    */
end_comment

begin_decl_stmt
specifier|final
name|Object
name|walCacheLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|protected
name|RegionGroupingStrategy
name|strategy
init|=
literal|null
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
literal|null
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|String
name|providerId
init|=
literal|null
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
end_decl_stmt

begin_function
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
specifier|final
name|WALFactory
name|factory
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
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
name|listeners
operator|=
literal|null
operator|==
name|listeners
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|listeners
argument_list|)
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
name|conf
operator|=
name|conf
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * Populate the cache for this group.    */
end_comment

begin_function
name|FSHLog
name|populateCache
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isMeta
init|=
name|META_WAL_PROVIDER_ID
operator|.
name|equals
argument_list|(
name|providerId
argument_list|)
decl_stmt|;
name|String
name|hlogPrefix
decl_stmt|;
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
decl_stmt|;
if|if
condition|(
name|isMeta
condition|)
block|{
name|hlogPrefix
operator|=
name|this
operator|.
name|providerId
expr_stmt|;
comment|// don't watch log roll for meta
name|listeners
operator|=
name|Collections
operator|.
expr|<
name|WALActionsListener
operator|>
name|singletonList
argument_list|(
operator|new
name|MetricsWAL
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hlogPrefix
operator|=
name|groupName
expr_stmt|;
name|listeners
operator|=
name|this
operator|.
name|listeners
expr_stmt|;
block|}
name|FSHLog
name|log
init|=
operator|new
name|FSHLog
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|DefaultWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|providerId
argument_list|)
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
literal|true
argument_list|,
name|hlogPrefix
argument_list|,
name|isMeta
condition|?
name|META_WAL_PROVIDER_ID
else|:
literal|null
argument_list|)
decl_stmt|;
name|cached
operator|.
name|put
argument_list|(
name|groupName
argument_list|,
name|log
argument_list|)
expr_stmt|;
name|logs
operator|.
name|add
argument_list|(
name|log
argument_list|)
expr_stmt|;
return|return
name|log
return|;
block|}
end_function

begin_function
specifier|private
name|WAL
name|getWAL
parameter_list|(
specifier|final
name|String
name|group
parameter_list|)
throws|throws
name|IOException
block|{
name|WAL
name|log
init|=
name|cached
operator|.
name|get
argument_list|(
name|walCacheLock
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|log
condition|)
block|{
comment|// only lock when need to create wal, and need to lock since
comment|// creating hlog on fs is time consuming
synchronized|synchronized
init|(
name|this
operator|.
name|walCacheLock
init|)
block|{
name|log
operator|=
name|cached
operator|.
name|get
argument_list|(
name|group
argument_list|)
expr_stmt|;
comment|// check again
if|if
condition|(
literal|null
operator|==
name|log
condition|)
block|{
name|log
operator|=
name|populateCache
argument_list|(
name|group
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|log
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
specifier|final
name|byte
index|[]
name|identifier
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
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
name|group
operator|=
name|strategy
operator|.
name|group
argument_list|(
name|identifier
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
synchronized|synchronized
init|(
name|logs
init|)
block|{
for|for
control|(
name|FSHLog
name|wal
range|:
name|logs
control|)
block|{
try|try
block|{
name|wal
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem shutting down log '"
operator|+
name|wal
operator|+
literal|"': "
operator|+
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Details of problem shutting down log '"
operator|+
name|wal
operator|+
literal|"'"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|failure
operator|=
name|exception
expr_stmt|;
block|}
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
synchronized|synchronized
init|(
name|logs
init|)
block|{
for|for
control|(
name|FSHLog
name|wal
range|:
name|logs
control|)
block|{
try|try
block|{
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem closing log '"
operator|+
name|wal
operator|+
literal|"': "
operator|+
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Details of problem closing wal '"
operator|+
name|wal
operator|+
literal|"'"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|failure
operator|=
name|exception
expr_stmt|;
block|}
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
synchronized|synchronized
init|(
name|logs
init|)
block|{
for|for
control|(
name|FSHLog
name|wal
range|:
name|logs
control|)
block|{
name|numLogFiles
operator|+=
name|wal
operator|.
name|getNumLogFiles
argument_list|()
expr_stmt|;
block|}
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
synchronized|synchronized
init|(
name|logs
init|)
block|{
for|for
control|(
name|FSHLog
name|wal
range|:
name|logs
control|)
block|{
name|logFileSize
operator|+=
name|wal
operator|.
name|getLogFileSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|logFileSize
return|;
block|}
end_function

unit|}
end_unit

