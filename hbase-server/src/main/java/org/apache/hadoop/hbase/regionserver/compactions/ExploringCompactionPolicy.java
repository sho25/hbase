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
name|regionserver
operator|.
name|compactions
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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|hbase
operator|.
name|regionserver
operator|.
name|StoreConfigInformation
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
name|StoreFile
import|;
end_import

begin_comment
comment|/**  * Class to pick which files if any to compact together.  *  * This class will search all possibilities for different and if it gets stuck it will choose  * the smallest set of files to compact.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExploringCompactionPolicy
extends|extends
name|RatioBasedCompactionPolicy
block|{
comment|/**    * Constructor for ExploringCompactionPolicy.    * @param conf The configuration object    * @param storeConfigInfo An object to provide info about the store.    */
specifier|public
name|ExploringCompactionPolicy
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|final
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|applyCompactionPolicy
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
specifier|final
name|boolean
name|mayUseOffPeak
parameter_list|,
specifier|final
name|boolean
name|mightBeStuck
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Start off choosing nothing.
name|List
argument_list|<
name|StoreFile
argument_list|>
name|bestSelection
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|smallest
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|bestSize
init|=
literal|0
decl_stmt|;
name|long
name|smallestSize
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// Consider every starting place.
for|for
control|(
name|int
name|start
init|=
literal|0
init|;
name|start
operator|<
name|candidates
operator|.
name|size
argument_list|()
condition|;
name|start
operator|++
control|)
block|{
comment|// Consider every different sub list permutation in between start and end with min files.
for|for
control|(
name|int
name|currentEnd
init|=
name|start
operator|+
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
operator|-
literal|1
init|;
name|currentEnd
operator|<
name|candidates
operator|.
name|size
argument_list|()
condition|;
name|currentEnd
operator|++
control|)
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|potentialMatchFiles
init|=
name|candidates
operator|.
name|subList
argument_list|(
name|start
argument_list|,
name|currentEnd
operator|+
literal|1
argument_list|)
decl_stmt|;
comment|// Sanity checks
if|if
condition|(
name|potentialMatchFiles
operator|.
name|size
argument_list|()
operator|<
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|potentialMatchFiles
operator|.
name|size
argument_list|()
operator|>
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Compute the total size of files that will
comment|// have to be read if this set of files is compacted.
name|long
name|size
init|=
name|getTotalStoreSize
argument_list|(
name|potentialMatchFiles
argument_list|)
decl_stmt|;
comment|// Store the smallest set of files.  This stored set of files will be used
comment|// if it looks like the algorithm is stuck.
if|if
condition|(
name|size
operator|<
name|smallestSize
condition|)
block|{
name|smallest
operator|=
name|potentialMatchFiles
expr_stmt|;
name|smallestSize
operator|=
name|size
expr_stmt|;
block|}
if|if
condition|(
name|size
operator|>=
name|comConf
operator|.
name|getMinCompactSize
argument_list|()
operator|&&
operator|!
name|filesInRatio
argument_list|(
name|potentialMatchFiles
argument_list|,
name|mayUseOffPeak
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|size
operator|>
name|comConf
operator|.
name|getMaxCompactSize
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Keep if this gets rid of more files.  Or the same number of files for less io.
if|if
condition|(
name|potentialMatchFiles
operator|.
name|size
argument_list|()
operator|>
name|bestSelection
operator|.
name|size
argument_list|()
operator|||
operator|(
name|potentialMatchFiles
operator|.
name|size
argument_list|()
operator|==
name|bestSelection
operator|.
name|size
argument_list|()
operator|&&
name|size
operator|<
name|bestSize
operator|)
condition|)
block|{
name|bestSelection
operator|=
name|potentialMatchFiles
expr_stmt|;
name|bestSize
operator|=
name|size
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|bestSelection
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|mightBeStuck
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|smallest
argument_list|)
return|;
block|}
return|return
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|bestSelection
argument_list|)
return|;
block|}
comment|/**    * Find the total size of a list of store files.    * @param potentialMatchFiles StoreFile list.    * @return Sum of StoreFile.getReader().length();    */
specifier|private
name|long
name|getTotalStoreSize
parameter_list|(
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|potentialMatchFiles
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|s
range|:
name|potentialMatchFiles
control|)
block|{
name|size
operator|+=
name|s
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
comment|/**    * Check that all files satisfy the constraint    *      FileSize(i)<= ( Sum(0,N,FileSize(_)) - FileSize(i) ) * Ratio.    *    * @param files List of store files to consider as a compaction candidate.    * @param isOffPeak should the offPeak compaction ratio be used ?    * @return a boolean if these files satisfy the ratio constraints.    */
specifier|private
name|boolean
name|filesInRatio
parameter_list|(
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
specifier|final
name|boolean
name|isOffPeak
parameter_list|)
block|{
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
return|return
literal|true
return|;
block|}
specifier|final
name|double
name|currentRatio
init|=
name|isOffPeak
condition|?
name|comConf
operator|.
name|getCompactionRatioOffPeak
argument_list|()
else|:
name|comConf
operator|.
name|getCompactionRatio
argument_list|()
decl_stmt|;
name|long
name|totalFileSize
init|=
name|getTotalStoreSize
argument_list|(
name|files
argument_list|)
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|files
control|)
block|{
name|long
name|singleFileSize
init|=
name|file
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
name|long
name|sumAllOtherFileSizes
init|=
name|totalFileSize
operator|-
name|singleFileSize
decl_stmt|;
if|if
condition|(
name|singleFileSize
operator|>
name|sumAllOtherFileSizes
operator|*
name|currentRatio
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

