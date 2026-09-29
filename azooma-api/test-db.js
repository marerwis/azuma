const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
p.app_categories.findMany({ take: 3, select: { id: true, name: true } })
  .then(r => { console.log('categories OK:', JSON.stringify(r)); p.$disconnect(); })
  .catch(e => { console.error('error:', e.message); p.$disconnect(); });
