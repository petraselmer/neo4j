/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.impl.batchimport.store.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.neo4j.collection.pool.Pool;

import static org.neo4j.unsafe.impl.batchimport.store.BatchingPageCache.Writer;

class Funnel implements Writer
{
    private final Writer writer;
    private final WriteQueue queue;

    public Funnel( Writer writer, WriteQueue queue )
    {
        this.writer = writer;
        this.queue = queue;
    }

    @Override
    public void write( ByteBuffer byteBuffer, long position, Pool<ByteBuffer> poolToReleaseBufferIn )
            throws IOException
    {
        queue.offer( new WriteJob( writer, byteBuffer, position, poolToReleaseBufferIn ) );
    }
}
