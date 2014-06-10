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
name|StoreConfigInformation
import|;
end_import

begin_comment
comment|/**  * Compaction configuration for a particular instance of HStore.  * Takes into account both global settings and ones set on the column family/store.  * Control knobs for default compaction algorithm:  *<p/>  * maxCompactSize - upper bound on file size to be included in minor compactions  * minCompactSize - lower bound below which compaction is selected without ratio test  * minFilesToCompact - lower bound on number of files in any minor compaction  * maxFilesToCompact - upper bound on number of files in any minor compaction  * compactionRatio - Ratio used for compaction  *<p/>  * Set parameter as "hbase.hstore.compaction.<attribute>"  */
end_comment

begin_comment
comment|//TODO: revisit this class for online parameter updating (both in xml and on the CF)
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactionConfiguration
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactionConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_RATIO_KEY
init|=
literal|"hbase.hstore.compaction.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_RATIO_OFFPEAK_KEY
init|=
literal|"hbase.hstore.compaction.ratio.offpeak"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MIN_KEY
init|=
literal|"hbase.hstore.compaction.min"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MIN_SIZE_KEY
init|=
literal|"hbase.hstore.compaction.min.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MAX_KEY
init|=
literal|"hbase.hstore.compaction.max"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MAX_SIZE_KEY
init|=
literal|"hbase.hstore.compaction.max.size"
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|StoreConfigInformation
name|storeConfigInfo
decl_stmt|;
name|long
name|maxCompactSize
decl_stmt|;
name|long
name|minCompactSize
decl_stmt|;
name|int
name|minFilesToCompact
decl_stmt|;
name|int
name|maxFilesToCompact
decl_stmt|;
name|double
name|compactionRatio
decl_stmt|;
name|double
name|offPeekCompactionRatio
decl_stmt|;
name|long
name|throttlePoint
decl_stmt|;
name|long
name|majorCompactionPeriod
decl_stmt|;
name|float
name|majorCompactionJitter
decl_stmt|;
name|CompactionConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|storeConfigInfo
operator|=
name|storeConfigInfo
expr_stmt|;
name|maxCompactSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_MAX_SIZE_KEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|minCompactSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_MIN_SIZE_KEY
argument_list|,
name|storeConfigInfo
operator|.
name|getMemstoreFlushSize
argument_list|()
argument_list|)
expr_stmt|;
name|minFilesToCompact
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|2
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_HSTORE_COMPACTION_MIN_KEY
argument_list|,
comment|/*old name*/
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|maxFilesToCompact
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_HSTORE_COMPACTION_MAX_KEY
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|compactionRatio
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HBASE_HSTORE_COMPACTION_RATIO_KEY
argument_list|,
literal|1.2F
argument_list|)
expr_stmt|;
name|offPeekCompactionRatio
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HBASE_HSTORE_COMPACTION_RATIO_OFFPEAK_KEY
argument_list|,
literal|5.0F
argument_list|)
expr_stmt|;
name|throttlePoint
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.thread.compaction.throttle"
argument_list|,
literal|2
operator|*
name|maxFilesToCompact
operator|*
name|storeConfigInfo
operator|.
name|getMemstoreFlushSize
argument_list|()
argument_list|)
expr_stmt|;
name|majorCompactionPeriod
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|1000
operator|*
literal|60
operator|*
literal|60
operator|*
literal|24
operator|*
literal|7
argument_list|)
expr_stmt|;
comment|// Make it 0.5 so jitter has us fall evenly either side of when the compaction should run
name|majorCompactionJitter
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
literal|0.50F
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"size [%d, %d); files [%d, %d); ratio %f; off-peak ratio %f; throttle point %d;"
operator|+
literal|" major period %d, major jitter %f"
argument_list|,
name|minCompactSize
argument_list|,
name|maxCompactSize
argument_list|,
name|minFilesToCompact
argument_list|,
name|maxFilesToCompact
argument_list|,
name|compactionRatio
argument_list|,
name|offPeekCompactionRatio
argument_list|,
name|throttlePoint
argument_list|,
name|majorCompactionPeriod
argument_list|,
name|majorCompactionJitter
argument_list|)
return|;
block|}
comment|/**    * @return lower bound below which compaction is selected without ratio test    */
name|long
name|getMinCompactSize
parameter_list|()
block|{
return|return
name|minCompactSize
return|;
block|}
comment|/**    * @return upper bound on file size to be included in minor compactions    */
name|long
name|getMaxCompactSize
parameter_list|()
block|{
return|return
name|maxCompactSize
return|;
block|}
comment|/**    * @return upper bound on number of files to be included in minor compactions    */
name|int
name|getMinFilesToCompact
parameter_list|()
block|{
return|return
name|minFilesToCompact
return|;
block|}
comment|/**    * @return upper bound on number of files to be included in minor compactions    */
name|int
name|getMaxFilesToCompact
parameter_list|()
block|{
return|return
name|maxFilesToCompact
return|;
block|}
comment|/**    * @return Ratio used for compaction    */
name|double
name|getCompactionRatio
parameter_list|()
block|{
return|return
name|compactionRatio
return|;
block|}
comment|/**    * @return Off peak Ratio used for compaction    */
name|double
name|getCompactionRatioOffPeak
parameter_list|()
block|{
return|return
name|offPeekCompactionRatio
return|;
block|}
comment|/**    * @return ThrottlePoint used for classifying small and large compactions    */
name|long
name|getThrottlePoint
parameter_list|()
block|{
return|return
name|throttlePoint
return|;
block|}
comment|/**    * @return Major compaction period from compaction.    * Major compactions are selected periodically according to this parameter plus jitter    */
name|long
name|getMajorCompactionPeriod
parameter_list|()
block|{
return|return
name|majorCompactionPeriod
return|;
block|}
comment|/**    * @return Major the jitter fraction, the fraction within which the major compaction    *  period is randomly chosen from the majorCompactionPeriod in each store.    */
name|float
name|getMajorCompactionJitter
parameter_list|()
block|{
return|return
name|majorCompactionJitter
return|;
block|}
block|}
end_class

end_unit

