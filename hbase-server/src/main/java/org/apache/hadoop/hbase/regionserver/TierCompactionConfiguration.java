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
package|;
end_package

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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DecimalFormat
import|;
end_import

begin_comment
comment|/**  * Control knobs for default compaction algorithm  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TierCompactionConfiguration
extends|extends
name|CompactionConfiguration
block|{
specifier|private
name|CompactionTier
index|[]
name|compactionTier
decl_stmt|;
specifier|private
name|boolean
name|recentFirstOrder
decl_stmt|;
name|TierCompactionConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
name|String
name|strPrefix
init|=
literal|"hbase.hstore.compaction."
decl_stmt|;
name|String
name|strSchema
init|=
literal|"tbl."
operator|+
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"cf."
operator|+
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"."
decl_stmt|;
name|String
name|strDefault
init|=
literal|"Default."
decl_stmt|;
name|String
name|strAttribute
decl_stmt|;
comment|// If value not set for family, use default family (by passing null).
comment|// If default value not set, use 1 tier.
name|strAttribute
operator|=
literal|"NumCompactionTiers"
expr_stmt|;
name|compactionTier
operator|=
operator|new
name|CompactionTier
index|[
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
literal|1
argument_list|)
argument_list|)
index|]
expr_stmt|;
name|strAttribute
operator|=
literal|"IsRecentFirstOrder"
expr_stmt|;
name|recentFirstOrder
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MinCompactSize"
expr_stmt|;
name|minCompactSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MaxCompactSize"
expr_stmt|;
name|maxCompactSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"ShouldDeleteExpired"
expr_stmt|;
name|shouldDeleteExpired
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
name|shouldDeleteExpired
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"ThrottlePoint"
expr_stmt|;
name|throttlePoint
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
name|throttlePoint
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MajorCompactionPeriod"
expr_stmt|;
name|majorCompactionPeriod
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
name|majorCompactionPeriod
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MajorCompactionJitter"
expr_stmt|;
name|majorCompactionJitter
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strAttribute
argument_list|,
name|majorCompactionJitter
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|compactionTier
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|compactionTier
index|[
name|i
index|]
operator|=
operator|new
name|CompactionTier
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return Number of compaction Tiers    */
name|int
name|getNumCompactionTiers
parameter_list|()
block|{
return|return
name|compactionTier
operator|.
name|length
return|;
block|}
comment|/**    * @return The i-th tier from most recent    */
name|CompactionTier
name|getCompactionTier
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|compactionTier
index|[
name|i
index|]
return|;
block|}
comment|/**    * @return Whether the tiers will be checked for compaction from newest to oldest    */
name|boolean
name|isRecentFirstOrder
parameter_list|()
block|{
return|return
name|recentFirstOrder
return|;
block|}
comment|/**    * Parameters for each tier    */
class|class
name|CompactionTier
block|{
specifier|private
name|long
name|maxAgeInDisk
decl_stmt|;
specifier|private
name|long
name|maxSize
decl_stmt|;
specifier|private
name|double
name|tierCompactionRatio
decl_stmt|;
specifier|private
name|int
name|tierMinFilesToCompact
decl_stmt|;
specifier|private
name|int
name|tierMaxFilesToCompact
decl_stmt|;
specifier|private
name|int
name|endingIndexForTier
decl_stmt|;
name|CompactionTier
parameter_list|(
name|int
name|tier
parameter_list|)
block|{
name|String
name|strPrefix
init|=
literal|"hbase.hstore.compaction."
decl_stmt|;
name|String
name|strSchema
init|=
literal|"tbl."
operator|+
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"cf."
operator|+
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"."
decl_stmt|;
name|String
name|strDefault
init|=
literal|"Default."
decl_stmt|;
name|String
name|strDefTier
init|=
literal|""
decl_stmt|;
name|String
name|strTier
init|=
literal|"Tier."
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|tier
argument_list|)
operator|+
literal|"."
decl_stmt|;
name|String
name|strAttribute
decl_stmt|;
comment|/**        * Use value set for current family, current tier        * If not set, use value set for current family, default tier        * if not set, use value set for Default family, current tier        * If not set, use value set for Default family, default tier        * Else just use a default value        */
name|strAttribute
operator|=
literal|"MaxAgeInDisk"
expr_stmt|;
name|maxAgeInDisk
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MaxSize"
expr_stmt|;
name|maxSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"CompactionRatio"
expr_stmt|;
name|tierCompactionRatio
operator|=
operator|(
name|double
operator|)
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
operator|(
name|float
operator|)
name|compactionRatio
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MinFilesToCompact"
expr_stmt|;
name|tierMinFilesToCompact
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
name|minFilesToCompact
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"MaxFilesToCompact"
expr_stmt|;
name|tierMaxFilesToCompact
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strDefTier
operator|+
name|strAttribute
argument_list|,
name|maxFilesToCompact
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|strAttribute
operator|=
literal|"EndingIndexForTier"
expr_stmt|;
name|endingIndexForTier
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strSchema
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|strPrefix
operator|+
name|strDefault
operator|+
name|strTier
operator|+
name|strAttribute
argument_list|,
name|tier
argument_list|)
argument_list|)
expr_stmt|;
comment|//make sure this value is not incorrectly set
if|if
condition|(
name|endingIndexForTier
argument_list|<
literal|0
operator|||
name|endingIndexForTier
argument_list|>
name|tier
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"EndingIndexForTier improperly set. Using default value."
argument_list|)
expr_stmt|;
name|endingIndexForTier
operator|=
name|tier
expr_stmt|;
block|}
block|}
comment|/**      * @return Upper bound on storeFile's minFlushTime to be included in this tier      */
name|long
name|getMaxAgeInDisk
parameter_list|()
block|{
return|return
name|maxAgeInDisk
return|;
block|}
comment|/**      * @return Upper bound on storeFile's size to be included in this tier      */
name|long
name|getMaxSize
parameter_list|()
block|{
return|return
name|maxSize
return|;
block|}
comment|/**      * @return Compaction ratio for selections of this tier      */
name|double
name|getCompactionRatio
parameter_list|()
block|{
return|return
name|tierCompactionRatio
return|;
block|}
comment|/**      * @return lower bound on number of files in selections of this tier      */
name|int
name|getMinFilesToCompact
parameter_list|()
block|{
return|return
name|tierMinFilesToCompact
return|;
block|}
comment|/**      * @return upper bound on number of files in selections of this tier      */
name|int
name|getMaxFilesToCompact
parameter_list|()
block|{
return|return
name|tierMaxFilesToCompact
return|;
block|}
comment|/**      * @return the newest tier which will also be included in selections of this tier      *  by default it is the index of this tier, must be between 0 and this tier      */
name|int
name|getEndingIndexForTier
parameter_list|()
block|{
return|return
name|endingIndexForTier
return|;
block|}
name|String
name|getDescription
parameter_list|()
block|{
name|String
name|ageString
init|=
literal|"INF"
decl_stmt|;
name|String
name|sizeString
init|=
literal|"INF"
decl_stmt|;
if|if
condition|(
name|getMaxAgeInDisk
argument_list|()
operator|<
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|ageString
operator|=
name|StringUtils
operator|.
name|formatTime
argument_list|(
name|getMaxAgeInDisk
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getMaxSize
argument_list|()
operator|<
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|ageString
operator|=
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|ret
init|=
literal|"Has files upto age "
operator|+
name|ageString
operator|+
literal|" and upto size "
operator|+
name|sizeString
operator|+
literal|". "
operator|+
literal|"Compaction ratio: "
operator|+
operator|(
operator|new
name|DecimalFormat
argument_list|(
literal|"#.##"
argument_list|)
operator|)
operator|.
name|format
argument_list|(
name|getCompactionRatio
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
literal|"Compaction Selection with at least "
operator|+
name|getMinFilesToCompact
argument_list|()
operator|+
literal|" and "
operator|+
literal|"at most "
operator|+
name|getMaxFilesToCompact
argument_list|()
operator|+
literal|" files possible, "
operator|+
literal|"Selections in this tier includes files up to tier "
operator|+
name|getEndingIndexForTier
argument_list|()
decl_stmt|;
return|return
name|ret
return|;
block|}
block|}
block|}
end_class

end_unit

