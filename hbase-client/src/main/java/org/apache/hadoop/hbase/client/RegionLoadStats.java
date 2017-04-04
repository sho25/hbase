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
name|client
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
comment|/**  * POJO representing region server load  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RegionLoadStats
block|{
name|int
name|memstoreLoad
decl_stmt|;
name|int
name|heapOccupancy
decl_stmt|;
name|int
name|compactionPressure
decl_stmt|;
specifier|public
name|RegionLoadStats
parameter_list|(
name|int
name|memstoreLoad
parameter_list|,
name|int
name|heapOccupancy
parameter_list|,
name|int
name|compactionPressure
parameter_list|)
block|{
name|this
operator|.
name|memstoreLoad
operator|=
name|memstoreLoad
expr_stmt|;
name|this
operator|.
name|heapOccupancy
operator|=
name|heapOccupancy
expr_stmt|;
name|this
operator|.
name|compactionPressure
operator|=
name|compactionPressure
expr_stmt|;
block|}
specifier|public
name|int
name|getMemstoreLoad
parameter_list|()
block|{
return|return
name|this
operator|.
name|memstoreLoad
return|;
block|}
specifier|public
name|int
name|getHeapOccupancy
parameter_list|()
block|{
return|return
name|this
operator|.
name|heapOccupancy
return|;
block|}
specifier|public
name|int
name|getCompactionPressure
parameter_list|()
block|{
return|return
name|this
operator|.
name|compactionPressure
return|;
block|}
block|}
end_class

end_unit

