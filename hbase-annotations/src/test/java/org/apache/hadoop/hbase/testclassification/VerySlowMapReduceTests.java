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
name|testclassification
package|;
end_package

begin_comment
comment|/**  * Tag a test as related to  mapreduce and taking longer than 5 minutes to run on public build  * infrastructure.  *  * @see org.apache.hadoop.hbase.testclassification.ClientTests  * @see org.apache.hadoop.hbase.testclassification.CoprocessorTests  * @see org.apache.hadoop.hbase.testclassification.FilterTests  * @see org.apache.hadoop.hbase.testclassification.FlakeyTests  * @see org.apache.hadoop.hbase.testclassification.IOTests  * @see org.apache.hadoop.hbase.testclassification.MapReduceTests  * @see org.apache.hadoop.hbase.testclassification.MasterTests  * @see org.apache.hadoop.hbase.testclassification.MiscTests  * @see org.apache.hadoop.hbase.testclassification.RegionServerTests  * @see org.apache.hadoop.hbase.testclassification.ReplicationTests  * @see org.apache.hadoop.hbase.testclassification.RPCTests  * @see org.apache.hadoop.hbase.testclassification.SecurityTests  * @see org.apache.hadoop.hbase.testclassification.VerySlowRegionServerTests  * @see org.apache.hadoop.hbase.testclassification.VerySlowMapReduceTests  */
end_comment

begin_interface
specifier|public
interface|interface
name|VerySlowMapReduceTests
block|{ }
end_interface

end_unit

