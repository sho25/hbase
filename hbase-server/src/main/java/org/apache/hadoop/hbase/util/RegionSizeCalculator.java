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
operator|.
name|util
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|ClusterStatus
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
name|HRegionLocation
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
name|RegionLoad
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
name|ServerLoad
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|Admin
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
name|HBaseAdmin
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
name|HTable
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
name|RegionLocator
import|;
end_import

begin_comment
comment|/**  * Computes size of each region for given table and given column families.  * The value is used by MapReduce for better scheduling.  * */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionSizeCalculator
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
name|RegionSizeCalculator
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Maps each region to its size in bytes.    * */
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|sizeMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|ENABLE_REGIONSIZECALCULATOR
init|=
literal|"hbase.regionsizecalculator.enable"
decl_stmt|;
comment|/**    * Computes size of each region for table and given column families.    *     * @deprecated Use {@link #RegionSizeCalculator(RegionLocator, Admin)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|RegionSizeCalculator
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|table
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|init
argument_list|(
name|table
operator|.
name|getRegionLocator
argument_list|()
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Computes size of each region for table and given column families.    * */
specifier|public
name|RegionSizeCalculator
parameter_list|(
name|RegionLocator
name|regionLocator
parameter_list|,
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|regionLocator
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
name|RegionLocator
name|regionLocator
parameter_list|,
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|enabled
argument_list|(
name|admin
operator|.
name|getConfiguration
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region size calculation disabled."
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Calculating region sizes for table \""
operator|+
name|regionLocator
operator|.
name|getName
argument_list|()
operator|+
literal|"\"."
argument_list|)
expr_stmt|;
comment|//get regions for table
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|tableRegionInfos
init|=
name|regionLocator
operator|.
name|getAllRegionLocations
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|tableRegions
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|regionInfo
range|:
name|tableRegionInfos
control|)
block|{
name|tableRegions
operator|.
name|add
argument_list|(
name|regionInfo
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ClusterStatus
name|clusterStatus
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|clusterStatus
operator|.
name|getServers
argument_list|()
decl_stmt|;
specifier|final
name|long
name|megaByte
init|=
literal|1024L
operator|*
literal|1024L
decl_stmt|;
comment|//iterate all cluster regions, filter regions from our table and compute their size
for|for
control|(
name|ServerName
name|serverName
range|:
name|servers
control|)
block|{
name|ServerLoad
name|serverLoad
init|=
name|clusterStatus
operator|.
name|getLoad
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionLoad
name|regionLoad
range|:
name|serverLoad
operator|.
name|getRegionsLoad
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|byte
index|[]
name|regionId
init|=
name|regionLoad
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableRegions
operator|.
name|contains
argument_list|(
name|regionId
argument_list|)
condition|)
block|{
name|long
name|regionSizeBytes
init|=
name|regionLoad
operator|.
name|getStorefileSizeMB
argument_list|()
operator|*
name|megaByte
decl_stmt|;
name|sizeMap
operator|.
name|put
argument_list|(
name|regionId
argument_list|,
name|regionSizeBytes
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
literal|"Region "
operator|+
name|regionLoad
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" has size "
operator|+
name|regionSizeBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Region sizes calculated"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|enabled
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
return|return
name|configuration
operator|.
name|getBoolean
argument_list|(
name|ENABLE_REGIONSIZECALCULATOR
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Returns size of given region in bytes. Returns 0 if region was not found.    * */
specifier|public
name|long
name|getRegionSize
parameter_list|(
name|byte
index|[]
name|regionId
parameter_list|)
block|{
name|Long
name|size
init|=
name|sizeMap
operator|.
name|get
argument_list|(
name|regionId
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unknown region:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|regionId
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|size
return|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getRegionSizeMap
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|sizeMap
argument_list|)
return|;
block|}
block|}
end_class

end_unit

