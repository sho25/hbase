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
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseInterfaceAudience
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

begin_comment
comment|/**  * Data structure of three longs.  * Convenient package in which to carry current state of three counters.  *<p>Immutable!</p>  * @see MemStoreSizing  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
specifier|public
class|class
name|MemStoreSize
block|{
comment|/**    *'dataSize' tracks the Cell's data bytes size alone (Key bytes, value bytes). A cell's data can    * be in on heap or off heap area depending on the MSLAB and its configuration to be using on    * heap or off heap LABs    */
specifier|private
specifier|final
name|long
name|dataSize
decl_stmt|;
comment|/**'getHeapSize' tracks all Cell's heap size occupancy. This will include Cell POJO heap overhead.    * When Cells in on heap area, this will include the cells data size as well.    */
specifier|private
specifier|final
name|long
name|heapSize
decl_stmt|;
comment|/** off-heap size: the aggregated size of all data that is allocated off-heap including all    * key-values that reside off-heap and the metadata that resides off-heap    */
specifier|private
specifier|final
name|long
name|offHeapSize
decl_stmt|;
comment|/**    * Package private constructor.    */
name|MemStoreSize
parameter_list|()
block|{
name|this
argument_list|(
literal|0L
argument_list|,
literal|0L
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
comment|/**    * Package private constructor.    */
name|MemStoreSize
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
name|this
operator|.
name|dataSize
operator|=
name|dataSize
expr_stmt|;
name|this
operator|.
name|heapSize
operator|=
name|heapSize
expr_stmt|;
name|this
operator|.
name|offHeapSize
operator|=
name|offHeapSize
expr_stmt|;
block|}
comment|/**    * Package private constructor.    */
name|MemStoreSize
parameter_list|(
name|MemStoreSize
name|memStoreSize
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|=
name|memStoreSize
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|heapSize
operator|=
name|memStoreSize
operator|.
name|getHeapSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|offHeapSize
operator|=
name|memStoreSize
operator|.
name|getOffHeapSize
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataSize
operator|==
literal|0
operator|&&
name|this
operator|.
name|heapSize
operator|==
literal|0
operator|&&
name|this
operator|.
name|offHeapSize
operator|==
literal|0
return|;
block|}
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataSize
return|;
block|}
specifier|public
name|long
name|getHeapSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|heapSize
return|;
block|}
specifier|public
name|long
name|getOffHeapSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|offHeapSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|MemStoreSize
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MemStoreSize
name|other
init|=
operator|(
name|MemStoreSize
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|dataSize
operator|==
name|other
operator|.
name|dataSize
operator|&&
name|this
operator|.
name|heapSize
operator|==
name|other
operator|.
name|heapSize
operator|&&
name|this
operator|.
name|offHeapSize
operator|==
name|other
operator|.
name|offHeapSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|long
name|h
init|=
literal|31
operator|*
name|this
operator|.
name|dataSize
decl_stmt|;
name|h
operator|=
name|h
operator|+
literal|31
operator|*
name|this
operator|.
name|heapSize
expr_stmt|;
name|h
operator|=
name|h
operator|+
literal|31
operator|*
name|this
operator|.
name|offHeapSize
expr_stmt|;
return|return
operator|(
name|int
operator|)
name|h
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
literal|"dataSize="
operator|+
name|this
operator|.
name|dataSize
operator|+
literal|", getHeapSize="
operator|+
name|this
operator|.
name|heapSize
operator|+
literal|", getOffHeapSize="
operator|+
name|this
operator|.
name|offHeapSize
return|;
block|}
block|}
end_class

end_unit

