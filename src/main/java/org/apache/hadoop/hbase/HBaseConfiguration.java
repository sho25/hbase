begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|util
operator|.
name|VersionInfo
import|;
end_import

begin_comment
comment|/**  * Adds HBase configuration files to a Configuration  */
end_comment

begin_class
specifier|public
class|class
name|HBaseConfiguration
extends|extends
name|Configuration
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
name|HBaseConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// a constant to convert a fraction to a percentage
specifier|private
specifier|static
specifier|final
name|int
name|CONVERT_TO_PERCENTAGE
init|=
literal|100
decl_stmt|;
comment|/**    * Instantinating HBaseConfiguration() is deprecated. Please use    * HBaseConfiguration#create() to construct a plain Configuration    */
annotation|@
name|Deprecated
specifier|public
name|HBaseConfiguration
parameter_list|()
block|{
comment|//TODO:replace with private constructor, HBaseConfiguration should not extend Configuration
name|super
argument_list|()
expr_stmt|;
name|addHbaseResources
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"instantiating HBaseConfiguration() is deprecated. Please use"
operator|+
literal|" HBaseConfiguration#create() to construct a plain Configuration"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiating HBaseConfiguration() is deprecated. Please use    * HBaseConfiguration#create(conf) to construct a plain Configuration    */
annotation|@
name|Deprecated
specifier|public
name|HBaseConfiguration
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
comment|//TODO:replace with private constructor
name|this
argument_list|()
expr_stmt|;
name|merge
argument_list|(
name|this
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkDefaultsVersion
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
literal|true
condition|)
return|return;
comment|// REMOVE
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.defaults.for.version.skip"
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
condition|)
return|return;
name|String
name|defaultsVersion
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.defaults.for.version"
argument_list|)
decl_stmt|;
name|String
name|thisVersion
init|=
name|VersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|thisVersion
operator|.
name|equals
argument_list|(
name|defaultsVersion
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"hbase-default.xml file seems to be for and old version of HBase ("
operator|+
name|defaultsVersion
operator|+
literal|"), this version is "
operator|+
name|thisVersion
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkForClusterFreeMemoryLimit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|float
name|globalMemstoreLimit
init|=
name|conf
operator|.
name|getFloat
argument_list|(
literal|"hbase.regionserver.global.memstore.upperLimit"
argument_list|,
literal|0.4f
argument_list|)
decl_stmt|;
name|int
name|gml
init|=
call|(
name|int
call|)
argument_list|(
name|globalMemstoreLimit
operator|*
name|CONVERT_TO_PERCENTAGE
argument_list|)
decl_stmt|;
name|float
name|blockCacheUpperLimit
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|bcul
init|=
call|(
name|int
call|)
argument_list|(
name|blockCacheUpperLimit
operator|*
name|CONVERT_TO_PERCENTAGE
argument_list|)
decl_stmt|;
if|if
condition|(
name|CONVERT_TO_PERCENTAGE
operator|-
operator|(
name|gml
operator|+
name|bcul
operator|)
operator|<
call|(
name|int
call|)
argument_list|(
name|CONVERT_TO_PERCENTAGE
operator|*
name|HConstants
operator|.
name|HBASE_CLUSTER_MINIMUM_MEMORY_THRESHOLD
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Current heap configuration for MemStore and BlockCache exceeds "
operator|+
literal|"the threshold required for successful cluster operation. "
operator|+
literal|"The combined value cannot exceed 0.8. Please check "
operator|+
literal|"the settings for hbase.regionserver.global.memstore.upperLimit and "
operator|+
literal|"hfile.block.cache.size in your configuration. "
operator|+
literal|"hbase.regionserver.global.memstore.upperLimit is "
operator|+
name|globalMemstoreLimit
operator|+
literal|" hfile.block.cache.size is "
operator|+
name|blockCacheUpperLimit
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|Configuration
name|addHbaseResources
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|addResource
argument_list|(
literal|"hbase-default.xml"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
literal|"hbase-site.xml"
argument_list|)
expr_stmt|;
name|checkDefaultsVersion
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|checkForClusterFreeMemoryLimit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Creates a Configuration with HBase resources    * @return a Configuration with HBase resources    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
return|return
name|addHbaseResources
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * Creates a clone of passed configuration.    * @param that Configuration to clone.    * @return a clone of passed configuration.    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|(
specifier|final
name|Configuration
name|that
parameter_list|)
block|{
return|return
operator|new
name|Configuration
argument_list|(
name|that
argument_list|)
return|;
block|}
comment|/**    * Merge two configurations.    * @param destConf the configuration that will be overwritten with items    *                 from the srcConf    * @param srcConf the source configuration    **/
specifier|public
specifier|static
name|void
name|merge
parameter_list|(
name|Configuration
name|destConf
parameter_list|,
name|Configuration
name|srcConf
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|srcConf
control|)
block|{
name|destConf
operator|.
name|set
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    *     * @return whether to show HBase Configuration in servlet    */
specifier|public
specifier|static
name|boolean
name|isShowConfInServlet
parameter_list|()
block|{
name|boolean
name|isShowConf
init|=
literal|false
decl_stmt|;
try|try
block|{
if|if
condition|(
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.conf.ConfServlet"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|isShowConf
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{            }
return|return
name|isShowConf
return|;
block|}
block|}
end_class

end_unit

