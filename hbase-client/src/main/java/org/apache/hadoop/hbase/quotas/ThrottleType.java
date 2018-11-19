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
name|quotas
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
comment|/**  * Describe the Throttle Type.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|ThrottleType
block|{
comment|/** Throttling based on the number of requests per time-unit */
name|REQUEST_NUMBER
block|,
comment|/** Throttling based on the read+write data size */
name|REQUEST_SIZE
block|,
comment|/** Throttling based on the number of write requests per time-unit */
name|WRITE_NUMBER
block|,
comment|/** Throttling based on the write data size */
name|WRITE_SIZE
block|,
comment|/** Throttling based on the number of read requests per time-unit */
name|READ_NUMBER
block|,
comment|/** Throttling based on the read data size */
name|READ_SIZE
block|,
comment|/** Throttling based on the read+write capacity unit */
name|REQUEST_CAPACITY_UNIT
block|,
comment|/** Throttling based on the write data capacity unit */
name|WRITE_CAPACITY_UNIT
block|,
comment|/** Throttling based on the read data capacity unit */
name|READ_CAPACITY_UNIT
block|, }
end_enum

end_unit

