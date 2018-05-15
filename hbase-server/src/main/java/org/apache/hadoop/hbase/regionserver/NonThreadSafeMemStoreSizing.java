begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Accounting of current heap and data sizes.  *<em>NOT THREAD SAFE</em>.  * Use in a 'local' context only where just a single-thread is updating. No concurrency!  * Used, for example, when summing all Cells in a single batch where result is then applied to the  * Store.  * @see ThreadSafeMemStoreSizing  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|NonThreadSafeMemStoreSizing
implements|implements
name|MemStoreSizing
block|{
specifier|private
name|long
name|dataSize
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|heapSize
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|offHeapSize
init|=
literal|0
decl_stmt|;
name|NonThreadSafeMemStoreSizing
parameter_list|()
block|{
name|this
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|NonThreadSafeMemStoreSizing
parameter_list|(
name|MemStoreSize
name|mss
parameter_list|)
block|{
name|this
argument_list|(
name|mss
operator|.
name|getDataSize
argument_list|()
argument_list|,
name|mss
operator|.
name|getHeapSize
argument_list|()
argument_list|,
name|mss
operator|.
name|getOffHeapSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|NonThreadSafeMemStoreSizing
parameter_list|(
name|long
name|dataSize
parameter_list|,
name|long
name|heapSize
parameter_list|,
name|long
name|offHeapSize
parameter_list|)
block|{
name|incMemStoreSize
argument_list|(
name|dataSize
argument_list|,
name|heapSize
argument_list|,
name|offHeapSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MemStoreSize
name|getMemStoreSize
parameter_list|()
block|{
return|return
operator|new
name|MemStoreSize
argument_list|(
name|this
operator|.
name|dataSize
argument_list|,
name|this
operator|.
name|heapSize
argument_list|,
name|this
operator|.
name|offHeapSize
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|incMemStoreSize
parameter_list|(
name|long
name|dataSizeDelta
parameter_list|,
name|long
name|heapSizeDelta
parameter_list|,
name|long
name|offHeapSizeDelta
parameter_list|)
block|{
name|this
operator|.
name|offHeapSize
operator|+=
name|offHeapSizeDelta
expr_stmt|;
name|this
operator|.
name|heapSize
operator|+=
name|heapSizeDelta
expr_stmt|;
name|this
operator|.
name|dataSize
operator|+=
name|dataSizeDelta
expr_stmt|;
return|return
name|this
operator|.
name|dataSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|dataSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHeapSize
parameter_list|()
block|{
return|return
name|heapSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getOffHeapSize
parameter_list|()
block|{
return|return
name|offHeapSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getMemStoreSize
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit
