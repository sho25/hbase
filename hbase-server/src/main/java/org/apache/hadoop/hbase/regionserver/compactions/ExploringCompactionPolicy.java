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
specifier|public
name|ExploringCompactionPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
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
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|applyCompactionPolicy
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|mayUseOffPeak
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
name|long
name|bestSize
init|=
literal|0
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
continue|continue;
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
continue|continue;
if|if
condition|(
operator|!
name|filesInRatio
argument_list|(
name|potentialMatchFiles
argument_list|,
name|mayUseOffPeak
argument_list|)
condition|)
continue|continue;
comment|// Compute the total size of files that will
comment|// have to be read if this set of files is compacted.
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
comment|/**    * Check that all files satisfy the r    * @param files    * @return    */
specifier|private
name|boolean
name|filesInRatio
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
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
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|totalFileSize
operator|+=
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|long
name|singleFileSize
init|=
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
name|long
name|sumAllOtherFilesize
init|=
name|totalFileSize
operator|-
name|singleFileSize
decl_stmt|;
if|if
condition|(
operator|(
name|singleFileSize
operator|>
name|sumAllOtherFilesize
operator|*
name|currentRatio
operator|)
operator|&&
operator|(
name|sumAllOtherFilesize
operator|>=
name|comConf
operator|.
name|getMinCompactSize
argument_list|()
operator|)
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

