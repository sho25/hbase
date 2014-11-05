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
package|;
end_package

begin_comment
comment|/**  * Ways to keep cells marked for delete around.  */
end_comment

begin_comment
comment|/*  * Don't change the TRUE/FALSE labels below, these have to be called  * this way for backwards compatibility.  */
end_comment

begin_enum
specifier|public
enum|enum
name|KeepDeletedCells
block|{
comment|/** Deleted Cells are not retained. */
name|FALSE
block|,
comment|/**    * Deleted Cells are retained until they are removed by other means    * such TTL or VERSIONS.    * If no TTL is specified or no new versions of delete cells are    * written, they are retained forever.    */
name|TRUE
block|,
comment|/**    * Deleted Cells are retained until the delete marker expires due to TTL.    * This is useful when TTL is combined with MIN_VERSIONS and one    * wants to keep a minimum number of versions around but at the same    * time remove deleted cells after the TTL.    */
name|TTL
block|; }
end_enum

end_unit

