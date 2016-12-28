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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Wraps the data size part and heap overhead of the memstore.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemstoreSize
block|{
specifier|static
specifier|final
name|MemstoreSize
name|EMPTY_SIZE
init|=
operator|new
name|MemstoreSize
argument_list|()
decl_stmt|;
specifier|private
name|long
name|dataSize
decl_stmt|;
specifier|private
name|long
name|heapOverhead
decl_stmt|;
specifier|public
name|MemstoreSize
parameter_list|()
block|{
name|dataSize
operator|=
literal|0
expr_stmt|;
name|heapOverhead
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|MemstoreSize
parameter_list|(
name|long
name|dataSize
parameter_list|,
name|long
name|heapOverhead
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
name|heapOverhead
operator|=
name|heapOverhead
expr_stmt|;
block|}
specifier|public
name|void
name|incMemstoreSize
parameter_list|(
name|long
name|dataSize
parameter_list|,
name|long
name|heapOverhead
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|+=
name|dataSize
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|+=
name|heapOverhead
expr_stmt|;
block|}
specifier|public
name|void
name|incMemstoreSize
parameter_list|(
name|MemstoreSize
name|size
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|+=
name|size
operator|.
name|dataSize
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|+=
name|size
operator|.
name|heapOverhead
expr_stmt|;
block|}
specifier|public
name|void
name|decMemstoreSize
parameter_list|(
name|long
name|dataSize
parameter_list|,
name|long
name|heapOverhead
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|-=
name|dataSize
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|-=
name|heapOverhead
expr_stmt|;
block|}
specifier|public
name|void
name|decMemstoreSize
parameter_list|(
name|MemstoreSize
name|size
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|-=
name|size
operator|.
name|dataSize
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|-=
name|size
operator|.
name|heapOverhead
expr_stmt|;
block|}
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|dataSize
return|;
block|}
specifier|public
name|long
name|getHeapOverhead
parameter_list|()
block|{
return|return
name|heapOverhead
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
operator|||
operator|!
operator|(
name|obj
operator|instanceof
name|MemstoreSize
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MemstoreSize
name|other
init|=
operator|(
name|MemstoreSize
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
name|heapOverhead
operator|==
name|other
operator|.
name|heapOverhead
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
literal|13
operator|*
name|this
operator|.
name|dataSize
decl_stmt|;
name|h
operator|=
name|h
operator|+
literal|14
operator|*
name|this
operator|.
name|heapOverhead
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
literal|" , heapOverhead="
operator|+
name|this
operator|.
name|heapOverhead
return|;
block|}
block|}
end_class

end_unit

