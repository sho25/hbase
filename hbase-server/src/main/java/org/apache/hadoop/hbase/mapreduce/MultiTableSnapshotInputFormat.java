begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|fs
operator|.
name|Path
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
name|hbase
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
name|hbase
operator|.
name|client
operator|.
name|Scan
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|JobContext
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * MultiTableSnapshotInputFormat generalizes  * {@link org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat}  * allowing a MapReduce job to run over one or more table snapshots, with one or more scans  * configured for each.  * Internally, the input format delegates to  * {@link org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat}  * and thus has the same performance advantages;  * see {@link org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat} for  * more details.  * Usage is similar to TableSnapshotInputFormat, with the following exception:  * initMultiTableSnapshotMapperJob takes in a map  * from snapshot name to a collection of scans. For each snapshot in the map, each corresponding  * scan will be applied;  * the overall dataset for the job is defined by the concatenation of the regions and tables  * included in each snapshot/scan  * pair.  * {@link org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil#initMultiTableSnapshotMapperJob  * (java.util.Map, Class, Class, Class, org.apache.hadoop.mapreduce.Job, boolean, org.apache  * .hadoop.fs.Path)}  * can be used to configure the job.  *<pre>{@code  * Job job = new Job(conf);  * Map<String, Collection<Scan>> snapshotScans = ImmutableMap.of(  *    "snapshot1", ImmutableList.of(new Scan(Bytes.toBytes("a"), Bytes.toBytes("b"))),  *    "snapshot2", ImmutableList.of(new Scan(Bytes.toBytes("1"), Bytes.toBytes("2")))  * );  * Path restoreDir = new Path("/tmp/snapshot_restore_dir")  * TableMapReduceUtil.initTableSnapshotMapperJob(  *     snapshotScans, MyTableMapper.class, MyMapKeyOutput.class,  *      MyMapOutputValueWritable.class, job, true, restoreDir);  * }  *</pre>  * Internally, this input format restores each snapshot into a subdirectory of the given tmp  * directory. Input splits and  * record readers are created as described in {@link org.apache.hadoop.hbase.mapreduce  * .TableSnapshotInputFormat}  * (one per region).  * See {@link org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat} for more notes on  * permissioning; the  * same caveats apply here.  *  * @see org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat  * @see org.apache.hadoop.hbase.client.TableSnapshotScanner  */
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
name|MultiTableSnapshotInputFormat
extends|extends
name|TableSnapshotInputFormat
block|{
specifier|private
specifier|final
name|MultiTableSnapshotInputFormatImpl
name|delegate
decl_stmt|;
specifier|public
name|MultiTableSnapshotInputFormat
parameter_list|()
block|{
name|this
operator|.
name|delegate
operator|=
operator|new
name|MultiTableSnapshotInputFormatImpl
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|>
name|splits
init|=
name|delegate
operator|.
name|getSplits
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|rtn
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|splits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
name|split
range|:
name|splits
control|)
block|{
name|rtn
operator|.
name|add
argument_list|(
operator|new
name|TableSnapshotInputFormat
operator|.
name|TableSnapshotRegionSplit
argument_list|(
name|split
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rtn
return|;
block|}
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|snapshotScans
parameter_list|,
name|Path
name|tmpRestoreDir
parameter_list|)
throws|throws
name|IOException
block|{
operator|new
name|MultiTableSnapshotInputFormatImpl
argument_list|()
operator|.
name|setInput
argument_list|(
name|configuration
argument_list|,
name|snapshotScans
argument_list|,
name|tmpRestoreDir
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

