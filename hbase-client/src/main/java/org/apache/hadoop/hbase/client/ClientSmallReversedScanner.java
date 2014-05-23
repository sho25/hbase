begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|client
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
name|Cell
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
name|KeyValueUtil
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
name|TableName
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Client scanner for small reversed scan. Generally, only one RPC is called to fetch the  * scan results, unless the results cross multiple regions or the row count of  * results exceed the caching.  *<p/>  * For small scan, it will get better performance than {@link ReversedClientScanner}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ClientSmallReversedScanner
extends|extends
name|ReversedClientScanner
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
name|ClientSmallReversedScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionServerCallable
argument_list|<
name|Result
index|[]
argument_list|>
name|smallScanCallable
init|=
literal|null
decl_stmt|;
specifier|private
name|byte
index|[]
name|skipRowOfFirstResult
init|=
literal|null
decl_stmt|;
comment|/**    * Create a new ReversibleClientScanner for the specified table Note that the    * passed {@link org.apache.hadoop.hbase.client.Scan}'s start row maybe changed.    *    * @param conf       The {@link org.apache.hadoop.conf.Configuration} to use.    * @param scan       {@link org.apache.hadoop.hbase.client.Scan} to use in this scanner    * @param tableName  The table that we wish to scan    * @param connection Connection identifying the cluster    * @throws java.io.IOException    */
specifier|public
name|ClientSmallReversedScanner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|HConnection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
comment|/**    * Gets a scanner for following scan. Move to next region or continue from the    * last result or start from the start row.    *    * @param nbRows    * @param done              true if Server-side says we're done scanning.    * @param currentRegionDone true if scan is over on current region    * @return true if has next scanner    * @throws IOException    */
specifier|private
name|boolean
name|nextScanner
parameter_list|(
name|int
name|nbRows
parameter_list|,
specifier|final
name|boolean
name|done
parameter_list|,
name|boolean
name|currentRegionDone
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Where to start the next getter
name|byte
index|[]
name|localStartKey
decl_stmt|;
name|int
name|cacheNum
init|=
name|nbRows
decl_stmt|;
name|skipRowOfFirstResult
operator|=
literal|null
expr_stmt|;
comment|// if we're at end of table, close and return false to stop iterating
if|if
condition|(
name|this
operator|.
name|currentRegion
operator|!=
literal|null
operator|&&
name|currentRegionDone
condition|)
block|{
name|byte
index|[]
name|startKey
init|=
name|this
operator|.
name|currentRegion
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKey
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|startKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
operator|||
name|checkScanStopRow
argument_list|(
name|startKey
argument_list|)
operator|||
name|done
condition|)
block|{
name|close
argument_list|()
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
literal|"Finished with small scan at "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|// We take the row just under to get to the previous region.
name|localStartKey
operator|=
name|createClosestRowBefore
argument_list|(
name|startKey
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
literal|"Finished with region "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|lastResult
operator|!=
literal|null
condition|)
block|{
name|localStartKey
operator|=
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|skipRowOfFirstResult
operator|=
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|cacheNum
operator|++
expr_stmt|;
block|}
else|else
block|{
name|localStartKey
operator|=
name|this
operator|.
name|scan
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Advancing internal small scanner to startKey at '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|localStartKey
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|smallScanCallable
operator|=
name|ClientSmallScanner
operator|.
name|getSmallScanCallable
argument_list|(
name|scan
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|getTable
argument_list|()
argument_list|,
name|localStartKey
argument_list|,
name|cacheNum
argument_list|,
name|this
operator|.
name|rpcControllerFactory
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
operator|&&
name|skipRowOfFirstResult
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRegions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
comment|// If the scanner is closed and there's nothing left in the cache, next is a
comment|// no-op.
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|this
operator|.
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|Result
index|[]
name|values
init|=
literal|null
decl_stmt|;
name|long
name|remainingResultSize
init|=
name|maxScannerResultSize
decl_stmt|;
name|int
name|countdown
init|=
name|this
operator|.
name|caching
decl_stmt|;
name|boolean
name|currentRegionDone
init|=
literal|false
decl_stmt|;
comment|// Values == null means server-side filter has determined we must STOP
while|while
condition|(
name|remainingResultSize
operator|>
literal|0
operator|&&
name|countdown
operator|>
literal|0
operator|&&
name|nextScanner
argument_list|(
name|countdown
argument_list|,
name|values
operator|==
literal|null
argument_list|,
name|currentRegionDone
argument_list|)
condition|)
block|{
comment|// Server returns a null values if scanning is to stop. Else,
comment|// returns an empty array if scanning is to go on and we've just
comment|// exhausted current region.
name|values
operator|=
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|smallScanCallable
argument_list|,
name|scannerTimeout
argument_list|)
expr_stmt|;
name|this
operator|.
name|currentRegion
operator|=
name|smallScanCallable
operator|.
name|getHRegionInfo
argument_list|()
expr_stmt|;
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|sumOfMillisSecBetweenNexts
operator|.
name|addAndGet
argument_list|(
name|currentTime
operator|-
name|lastNext
argument_list|)
expr_stmt|;
block|}
name|lastNext
operator|=
name|currentTime
expr_stmt|;
if|if
condition|(
name|values
operator|!=
literal|null
operator|&&
name|values
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|rs
init|=
name|values
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
operator|&&
name|this
operator|.
name|skipRowOfFirstResult
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|skipRowOfFirstResult
argument_list|,
name|rs
operator|.
name|getRow
argument_list|()
argument_list|)
condition|)
block|{
comment|// Skip the first result
continue|continue;
block|}
name|cache
operator|.
name|add
argument_list|(
name|rs
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|rs
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|remainingResultSize
operator|-=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|kv
argument_list|)
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
name|countdown
operator|--
expr_stmt|;
name|this
operator|.
name|lastResult
operator|=
name|rs
expr_stmt|;
block|}
block|}
name|currentRegionDone
operator|=
name|countdown
operator|>
literal|0
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|cache
operator|.
name|poll
argument_list|()
return|;
block|}
comment|// if we exhausted this scanner before calling close, write out the scan
comment|// metrics
name|writeScanMetrics
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeScannerInConstruction
parameter_list|()
throws|throws
name|IOException
block|{
comment|// No need to initialize the scanner when constructing instance, do it when
comment|// calling next(). Do nothing here.
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|scanMetricsPublished
condition|)
name|writeScanMetrics
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

