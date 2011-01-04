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
name|mapreduce
operator|.
name|hadoopbackport
package|;
end_package

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Random
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
name|conf
operator|.
name|Configured
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
name|FileSystem
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
name|io
operator|.
name|NullWritable
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
name|io
operator|.
name|RawComparator
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
name|io
operator|.
name|SequenceFile
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
name|io
operator|.
name|WritableComparable
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
name|InputFormat
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
name|Job
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
name|RecordReader
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
name|TaskAttemptContext
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
name|TaskAttemptID
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
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
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
name|util
operator|.
name|ReflectionUtils
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * Utility for collecting samples and writing a partition file for  * {@link TotalOrderPartitioner}.  *  * This is an identical copy of o.a.h.mapreduce.lib.partition.TotalOrderPartitioner  * from Hadoop trunk at r961542, with the exception of replacing  * TaskAttemptContextImpl with TaskAttemptContext.  */
end_comment

begin_class
specifier|public
class|class
name|InputSampler
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|Configured
implements|implements
name|Tool
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
name|InputSampler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|int
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"sampler -r<reduces>\n"
operator|+
literal|"      [-inFormat<input format class>]\n"
operator|+
literal|"      [-keyClass<map input& output key class>]\n"
operator|+
literal|"      [-splitRandom<double pcnt><numSamples><maxsplits> | "
operator|+
literal|"             // Sample from random splits at random (general)\n"
operator|+
literal|"       -splitSample<numSamples><maxsplits> | "
operator|+
literal|"             // Sample from first records in splits (random data)\n"
operator|+
literal|"       -splitInterval<double pcnt><maxsplits>]"
operator|+
literal|"             // Sample from splits at intervals (sorted data)"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Default sampler: -splitRandom 0.1 10000 10"
argument_list|)
expr_stmt|;
name|ToolRunner
operator|.
name|printGenericCommandUsage
argument_list|(
name|System
operator|.
name|out
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
specifier|public
name|InputSampler
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Interface to sample using an     * {@link org.apache.hadoop.mapreduce.InputFormat}.    */
specifier|public
interface|interface
name|Sampler
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
comment|/**      * For a given job, collect and return a subset of the keys from the      * input data.      */
name|K
index|[]
name|getSample
parameter_list|(
name|InputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|inf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
block|}
comment|/**    * Samples the first n records from s splits.    * Inexpensive way to sample random data.    */
specifier|public
specifier|static
class|class
name|SplitSampler
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Sampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|numSamples
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSplitsSampled
decl_stmt|;
comment|/**      * Create a SplitSampler sampling<em>all</em> splits.      * Takes the first numSamples / numSplits records from each split.      * @param numSamples Total number of samples to obtain from all selected      *                   splits.      */
specifier|public
name|SplitSampler
parameter_list|(
name|int
name|numSamples
parameter_list|)
block|{
name|this
argument_list|(
name|numSamples
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new SplitSampler.      * @param numSamples Total number of samples to obtain from all selected      *                   splits.      * @param maxSplitsSampled The maximum number of splits to examine.      */
specifier|public
name|SplitSampler
parameter_list|(
name|int
name|numSamples
parameter_list|,
name|int
name|maxSplitsSampled
parameter_list|)
block|{
name|this
operator|.
name|numSamples
operator|=
name|numSamples
expr_stmt|;
name|this
operator|.
name|maxSplitsSampled
operator|=
name|maxSplitsSampled
expr_stmt|;
block|}
comment|/**      * From each split sampled, take the first numSamples / numSplits records.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// ArrayList::toArray doesn't preserve type
specifier|public
name|K
index|[]
name|getSample
parameter_list|(
name|InputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|inf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|inf
operator|.
name|getSplits
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|K
argument_list|>
name|samples
init|=
operator|new
name|ArrayList
argument_list|<
name|K
argument_list|>
argument_list|(
name|numSamples
argument_list|)
decl_stmt|;
name|int
name|splitsToSample
init|=
name|Math
operator|.
name|min
argument_list|(
name|maxSplitsSampled
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|samplesPerSplit
init|=
name|numSamples
operator|/
name|splitsToSample
decl_stmt|;
name|long
name|records
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|splitsToSample
condition|;
operator|++
name|i
control|)
block|{
name|TaskAttemptContext
name|samplingContext
init|=
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|reader
init|=
name|inf
operator|.
name|createRecordReader
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
decl_stmt|;
name|reader
operator|.
name|initialize
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
expr_stmt|;
while|while
condition|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|samples
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|records
expr_stmt|;
if|if
condition|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|samplesPerSplit
operator|<=
name|records
condition|)
block|{
break|break;
block|}
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|K
index|[]
operator|)
name|samples
operator|.
name|toArray
argument_list|()
return|;
block|}
block|}
comment|/**    * Sample from random points in the input.    * General-purpose sampler. Takes numSamples / maxSplitsSampled inputs from    * each split.    */
specifier|public
specifier|static
class|class
name|RandomSampler
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Sampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
name|double
name|freq
decl_stmt|;
specifier|private
specifier|final
name|int
name|numSamples
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSplitsSampled
decl_stmt|;
comment|/**      * Create a new RandomSampler sampling<em>all</em> splits.      * This will read every split at the client, which is very expensive.      * @param freq Probability with which a key will be chosen.      * @param numSamples Total number of samples to obtain from all selected      *                   splits.      */
specifier|public
name|RandomSampler
parameter_list|(
name|double
name|freq
parameter_list|,
name|int
name|numSamples
parameter_list|)
block|{
name|this
argument_list|(
name|freq
argument_list|,
name|numSamples
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new RandomSampler.      * @param freq Probability with which a key will be chosen.      * @param numSamples Total number of samples to obtain from all selected      *                   splits.      * @param maxSplitsSampled The maximum number of splits to examine.      */
specifier|public
name|RandomSampler
parameter_list|(
name|double
name|freq
parameter_list|,
name|int
name|numSamples
parameter_list|,
name|int
name|maxSplitsSampled
parameter_list|)
block|{
name|this
operator|.
name|freq
operator|=
name|freq
expr_stmt|;
name|this
operator|.
name|numSamples
operator|=
name|numSamples
expr_stmt|;
name|this
operator|.
name|maxSplitsSampled
operator|=
name|maxSplitsSampled
expr_stmt|;
block|}
comment|/**      * Randomize the split order, then take the specified number of keys from      * each split sampled, where each key is selected with the specified      * probability and possibly replaced by a subsequently selected key when      * the quota of keys from that split is satisfied.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// ArrayList::toArray doesn't preserve type
specifier|public
name|K
index|[]
name|getSample
parameter_list|(
name|InputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|inf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|inf
operator|.
name|getSplits
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|K
argument_list|>
name|samples
init|=
operator|new
name|ArrayList
argument_list|<
name|K
argument_list|>
argument_list|(
name|numSamples
argument_list|)
decl_stmt|;
name|int
name|splitsToSample
init|=
name|Math
operator|.
name|min
argument_list|(
name|maxSplitsSampled
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|long
name|seed
init|=
name|r
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|r
operator|.
name|setSeed
argument_list|(
name|seed
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"seed: "
operator|+
name|seed
argument_list|)
expr_stmt|;
comment|// shuffle splits
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|splits
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|InputSplit
name|tmp
init|=
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|j
init|=
name|r
operator|.
name|nextInt
argument_list|(
name|splits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|splits
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|splits
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
name|splits
operator|.
name|set
argument_list|(
name|j
argument_list|,
name|tmp
argument_list|)
expr_stmt|;
block|}
comment|// our target rate is in terms of the maximum number of sample splits,
comment|// but we accept the possibility of sampling additional splits to hit
comment|// the target sample keyset
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|splitsToSample
operator|||
operator|(
name|i
operator|<
name|splits
operator|.
name|size
argument_list|()
operator|&&
name|samples
operator|.
name|size
argument_list|()
operator|<
name|numSamples
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|TaskAttemptContext
name|samplingContext
init|=
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|reader
init|=
name|inf
operator|.
name|createRecordReader
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
decl_stmt|;
name|reader
operator|.
name|initialize
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
expr_stmt|;
while|while
condition|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|r
operator|.
name|nextDouble
argument_list|()
operator|<=
name|freq
condition|)
block|{
if|if
condition|(
name|samples
operator|.
name|size
argument_list|()
operator|<
name|numSamples
condition|)
block|{
name|samples
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// When exceeding the maximum number of samples, replace a
comment|// random element with this one, then adjust the frequency
comment|// to reflect the possibility of existing elements being
comment|// pushed out
name|int
name|ind
init|=
name|r
operator|.
name|nextInt
argument_list|(
name|numSamples
argument_list|)
decl_stmt|;
if|if
condition|(
name|ind
operator|!=
name|numSamples
condition|)
block|{
name|samples
operator|.
name|set
argument_list|(
name|ind
argument_list|,
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|freq
operator|*=
operator|(
name|numSamples
operator|-
literal|1
operator|)
operator|/
operator|(
name|double
operator|)
name|numSamples
expr_stmt|;
block|}
block|}
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|K
index|[]
operator|)
name|samples
operator|.
name|toArray
argument_list|()
return|;
block|}
block|}
comment|/**    * Sample from s splits at regular intervals.    * Useful for sorted data.    */
specifier|public
specifier|static
class|class
name|IntervalSampler
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Sampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|final
name|double
name|freq
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSplitsSampled
decl_stmt|;
comment|/**      * Create a new IntervalSampler sampling<em>all</em> splits.      * @param freq The frequency with which records will be emitted.      */
specifier|public
name|IntervalSampler
parameter_list|(
name|double
name|freq
parameter_list|)
block|{
name|this
argument_list|(
name|freq
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new IntervalSampler.      * @param freq The frequency with which records will be emitted.      * @param maxSplitsSampled The maximum number of splits to examine.      * @see #getSample      */
specifier|public
name|IntervalSampler
parameter_list|(
name|double
name|freq
parameter_list|,
name|int
name|maxSplitsSampled
parameter_list|)
block|{
name|this
operator|.
name|freq
operator|=
name|freq
expr_stmt|;
name|this
operator|.
name|maxSplitsSampled
operator|=
name|maxSplitsSampled
expr_stmt|;
block|}
comment|/**      * For each split sampled, emit when the ratio of the number of records      * retained to the total record count is less than the specified      * frequency.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// ArrayList::toArray doesn't preserve type
specifier|public
name|K
index|[]
name|getSample
parameter_list|(
name|InputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|inf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|inf
operator|.
name|getSplits
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|K
argument_list|>
name|samples
init|=
operator|new
name|ArrayList
argument_list|<
name|K
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|splitsToSample
init|=
name|Math
operator|.
name|min
argument_list|(
name|maxSplitsSampled
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|records
init|=
literal|0
decl_stmt|;
name|long
name|kept
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|splitsToSample
condition|;
operator|++
name|i
control|)
block|{
name|TaskAttemptContext
name|samplingContext
init|=
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|reader
init|=
name|inf
operator|.
name|createRecordReader
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
decl_stmt|;
name|reader
operator|.
name|initialize
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|samplingContext
argument_list|)
expr_stmt|;
while|while
condition|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
operator|++
name|records
expr_stmt|;
if|if
condition|(
operator|(
name|double
operator|)
name|kept
operator|/
name|records
operator|<
name|freq
condition|)
block|{
name|samples
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|reader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|kept
expr_stmt|;
block|}
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|K
index|[]
operator|)
name|samples
operator|.
name|toArray
argument_list|()
return|;
block|}
block|}
comment|/**    * Write a partition file for the given job, using the Sampler provided.    * Queries the sampler for a sample keyset, sorts by the output key    * comparator, selects the keys for each rank, and writes to the destination    * returned from {@link TotalOrderPartitioner#getPartitionFile}.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// getInputFormat, getOutputKeyComparator
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|void
name|writePartitionFile
parameter_list|(
name|Job
name|job
parameter_list|,
name|Sampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|sampler
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
throws|,
name|InterruptedException
block|{
name|Configuration
name|conf
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|InputFormat
name|inf
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|job
operator|.
name|getInputFormatClass
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|int
name|numPartitions
init|=
name|job
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
name|K
index|[]
name|samples
init|=
name|sampler
operator|.
name|getSample
argument_list|(
name|inf
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using "
operator|+
name|samples
operator|.
name|length
operator|+
literal|" samples"
argument_list|)
expr_stmt|;
name|RawComparator
argument_list|<
name|K
argument_list|>
name|comparator
init|=
operator|(
name|RawComparator
argument_list|<
name|K
argument_list|>
operator|)
name|job
operator|.
name|getSortComparator
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|samples
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|Path
name|dst
init|=
operator|new
name|Path
argument_list|(
name|TotalOrderPartitioner
operator|.
name|getPartitionFile
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dst
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dst
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|dst
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|SequenceFile
operator|.
name|Writer
name|writer
init|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|dst
argument_list|,
name|job
operator|.
name|getMapOutputKeyClass
argument_list|()
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|)
decl_stmt|;
name|NullWritable
name|nullValue
init|=
name|NullWritable
operator|.
name|get
argument_list|()
decl_stmt|;
name|float
name|stepSize
init|=
name|samples
operator|.
name|length
operator|/
operator|(
name|float
operator|)
name|numPartitions
decl_stmt|;
name|int
name|last
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numPartitions
condition|;
operator|++
name|i
control|)
block|{
name|int
name|k
init|=
name|Math
operator|.
name|round
argument_list|(
name|stepSize
operator|*
name|i
argument_list|)
decl_stmt|;
while|while
condition|(
name|last
operator|>=
name|k
operator|&&
name|comparator
operator|.
name|compare
argument_list|(
name|samples
index|[
name|last
index|]
argument_list|,
name|samples
index|[
name|k
index|]
argument_list|)
operator|==
literal|0
condition|)
block|{
operator|++
name|k
expr_stmt|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|samples
index|[
name|k
index|]
argument_list|,
name|nullValue
argument_list|)
expr_stmt|;
name|last
operator|=
name|k
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Driver for InputSampler from the command line.    * Configures a JobConf instance and calls {@link #writePartitionFile}.    */
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|otherArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Sampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|sampler
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
if|if
condition|(
literal|"-r"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-inFormat"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|InputFormat
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-keyClass"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|WritableComparable
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-splitSample"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|int
name|numSamples
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
name|int
name|maxSplits
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|>=
name|maxSplits
condition|)
name|maxSplits
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|sampler
operator|=
operator|new
name|SplitSampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|numSamples
argument_list|,
name|maxSplits
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-splitRandom"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|double
name|pcnt
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
name|int
name|numSamples
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
name|int
name|maxSplits
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|>=
name|maxSplits
condition|)
name|maxSplits
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|sampler
operator|=
operator|new
name|RandomSampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|pcnt
argument_list|,
name|numSamples
argument_list|,
name|maxSplits
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-splitInterval"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|double
name|pcnt
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
name|int
name|maxSplits
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|>=
name|maxSplits
condition|)
name|maxSplits
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|sampler
operator|=
operator|new
name|IntervalSampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|pcnt
argument_list|,
name|maxSplits
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|otherArgs
operator|.
name|add
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|except
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Integer expected instead of "
operator|+
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
return|return
name|printUsage
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|except
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Required parameter missing from "
operator|+
name|args
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
return|return
name|printUsage
argument_list|()
return|;
block|}
block|}
if|if
condition|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
operator|<=
literal|1
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Sampler requires more than one reducer"
argument_list|)
expr_stmt|;
return|return
name|printUsage
argument_list|()
return|;
block|}
if|if
condition|(
name|otherArgs
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Wrong number of parameters: "
argument_list|)
expr_stmt|;
return|return
name|printUsage
argument_list|()
return|;
block|}
if|if
condition|(
literal|null
operator|==
name|sampler
condition|)
block|{
name|sampler
operator|=
operator|new
name|RandomSampler
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
literal|0.1
argument_list|,
literal|10000
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
name|Path
name|outf
init|=
operator|new
name|Path
argument_list|(
name|otherArgs
operator|.
name|remove
argument_list|(
name|otherArgs
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|TotalOrderPartitioner
operator|.
name|setPartitionFile
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|outf
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|otherArgs
control|)
block|{
name|FileInputFormat
operator|.
name|addInputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|InputSampler
operator|.
expr|<
name|K
operator|,
name|V
operator|>
name|writePartitionFile
argument_list|(
name|job
argument_list|,
name|sampler
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|InputSampler
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sampler
init|=
operator|new
name|InputSampler
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|res
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|sampler
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|res
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

