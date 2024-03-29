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
name|HashMap
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|BlockLocation
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
name|FileStatus
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
name|fs
operator|.
name|Path
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
comment|/**  * Thread that walks over the filesystem, and computes the mappings  * Region -> BestHost and Region -> {@code Map<HostName, fractional-locality-of-region>}  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|FSRegionScanner
implements|implements
name|Runnable
block|{
specifier|static
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FSRegionScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Path
name|regionPath
decl_stmt|;
comment|/**    * The file system used    */
specifier|private
name|FileSystem
name|fs
decl_stmt|;
comment|/**    * Maps each region to the RS with highest locality for that region.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|regionToBestLocalityRSMapping
decl_stmt|;
comment|/**    * Maps region encoded names to maps of hostnames to fractional locality of    * that region on that host.    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
argument_list|>
name|regionDegreeLocalityMapping
decl_stmt|;
name|FSRegionScanner
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|regionPath
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|regionToBestLocalityRSMapping
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
argument_list|>
name|regionDegreeLocalityMapping
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|regionPath
operator|=
name|regionPath
expr_stmt|;
name|this
operator|.
name|regionToBestLocalityRSMapping
operator|=
name|regionToBestLocalityRSMapping
expr_stmt|;
name|this
operator|.
name|regionDegreeLocalityMapping
operator|=
name|regionDegreeLocalityMapping
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// empty the map for each region
name|Map
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|blockCountMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|//get table name
name|String
name|tableName
init|=
name|regionPath
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|int
name|totalBlkCount
init|=
literal|0
decl_stmt|;
comment|// ignore null
name|FileStatus
index|[]
name|cfList
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|regionPath
argument_list|,
operator|new
name|FSUtils
operator|.
name|FamilyDirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|cfList
condition|)
block|{
return|return;
block|}
comment|// for each cf, get all the blocks information
for|for
control|(
name|FileStatus
name|cfStatus
range|:
name|cfList
control|)
block|{
if|if
condition|(
operator|!
name|cfStatus
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
comment|// skip because this is not a CF directory
continue|continue;
block|}
name|FileStatus
index|[]
name|storeFileLists
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|cfStatus
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|storeFileLists
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|FileStatus
name|storeFile
range|:
name|storeFileLists
control|)
block|{
name|BlockLocation
index|[]
name|blkLocations
init|=
name|fs
operator|.
name|getFileBlockLocations
argument_list|(
name|storeFile
argument_list|,
literal|0
argument_list|,
name|storeFile
operator|.
name|getLen
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|blkLocations
condition|)
block|{
continue|continue;
block|}
name|totalBlkCount
operator|+=
name|blkLocations
operator|.
name|length
expr_stmt|;
for|for
control|(
name|BlockLocation
name|blk
range|:
name|blkLocations
control|)
block|{
for|for
control|(
name|String
name|host
range|:
name|blk
operator|.
name|getHosts
argument_list|()
control|)
block|{
name|AtomicInteger
name|count
init|=
name|blockCountMap
operator|.
name|get
argument_list|(
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|==
literal|null
condition|)
block|{
name|count
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|blockCountMap
operator|.
name|put
argument_list|(
name|host
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|regionToBestLocalityRSMapping
operator|!=
literal|null
condition|)
block|{
name|int
name|largestBlkCount
init|=
literal|0
decl_stmt|;
name|String
name|hostToRun
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|entry
range|:
name|blockCountMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|host
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|int
name|tmp
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|tmp
operator|>
name|largestBlkCount
condition|)
block|{
name|largestBlkCount
operator|=
name|tmp
expr_stmt|;
name|hostToRun
operator|=
name|host
expr_stmt|;
block|}
block|}
comment|// empty regions could make this null
if|if
condition|(
literal|null
operator|==
name|hostToRun
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|hostToRun
operator|.
name|endsWith
argument_list|(
literal|"."
argument_list|)
condition|)
block|{
name|hostToRun
operator|=
name|hostToRun
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|hostToRun
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|String
name|name
init|=
name|tableName
operator|+
literal|":"
operator|+
name|regionPath
operator|.
name|getName
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|regionToBestLocalityRSMapping
init|)
block|{
name|regionToBestLocalityRSMapping
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|hostToRun
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|regionDegreeLocalityMapping
operator|!=
literal|null
operator|&&
name|totalBlkCount
operator|>
literal|0
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|hostLocalityMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|entry
range|:
name|blockCountMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|host
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|host
operator|.
name|endsWith
argument_list|(
literal|"."
argument_list|)
condition|)
block|{
name|host
operator|=
name|host
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|host
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Locality is fraction of blocks local to this host.
name|float
name|locality
init|=
operator|(
operator|(
name|float
operator|)
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
operator|)
operator|/
name|totalBlkCount
decl_stmt|;
name|hostLocalityMap
operator|.
name|put
argument_list|(
name|host
argument_list|,
name|locality
argument_list|)
expr_stmt|;
block|}
comment|// Put the locality map into the result map, keyed by the encoded name
comment|// of the region.
name|regionDegreeLocalityMapping
operator|.
name|put
argument_list|(
name|regionPath
operator|.
name|getName
argument_list|()
argument_list|,
name|hostLocalityMap
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Problem scanning file system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Problem scanning file system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

